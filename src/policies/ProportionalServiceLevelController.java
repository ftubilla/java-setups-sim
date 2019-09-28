package policies;

import java.util.Map;
import java.util.stream.StreamSupport;

import com.google.common.collect.Maps;

import lombok.extern.apachecommons.CommonsLog;
import metrics.surplusstatistics.StreamSurplusStatisticsCalculator;
import params.PolicyParams;
import sim.Clock;
import sim.Sim;
import system.Item;
import system.Machine;

/**
 * Implements a controller that may be used for matching a target service level by adjusting the
 * target surplus level of each item.
 *
 */
@CommonsLog
public class ProportionalServiceLevelController implements IServiceLevelController {

    private final Map<Item, StreamSurplusStatisticsCalculator> latestSurplusStats;
    private final Map<Item, Double> learnedServiceLevel;
    private final Map<Item, Double> targetServiceLevel;
    private final Map<Item, Double> itemPropGain;
    private final Map<Item, Integer> changeoversSinceLatestControl;
    private final Map<Item, Double> learningRate;
    private final double initialLearningRate;
    private final double learningRateDecayFactor;
    private final int changeoversPerControlCycle;
    private final Machine machine;
    private final Clock clock;

    public ProportionalServiceLevelController(final Sim sim) {

        PolicyParams params = sim.getParams().getPolicyParams();
        this.initialLearningRate = params.getServiceLevelControllerInitialLearningRate();
        this.learningRateDecayFactor = params.getServiceLevelControllerLearningRateDecayFactor();
        this.changeoversPerControlCycle = params.getServiceLevelControllerChangeoversPerCycle();
        this.clock = sim.getClock();
        this.machine = sim.getMachine();

        this.latestSurplusStats = Maps.newHashMap();
        this.learnedServiceLevel = Maps.newHashMap();
        this.targetServiceLevel = Maps.newHashMap();
        this.itemPropGain = Maps.newHashMap();
        this.changeoversSinceLatestControl = Maps.newHashMap();
        this.learningRate = Maps.newHashMap();

        double controllerPropGain = params.getServiceLevelControllerPropGain();

        // Get the average ideal deviation of the items to scale up the controller gain
        double aveIdealDev = StreamSupport.stream(sim.getMachine().spliterator(), false)
                .mapToDouble(i -> sim.getSurplusCostLowerBound().getIdealSurplusDeviation(i.getId()))
                .average().getAsDouble();

        for ( Item item : sim.getMachine() ) {
            // Initialize maps
            this.latestSurplusStats.put(item, new StreamSurplusStatisticsCalculator());
            this.learnedServiceLevel.put(item, 0.0);
            this.targetServiceLevel.put(item, sim.getDerivedParams().getServiceLevels().get(item.getId()));
            this.changeoversSinceLatestControl.put(item, 0);
            this.learningRate.put(item, this.initialLearningRate);
            // Set the controller gain to be proportional to the average ideal surplus deviation
            double itemGain = aveIdealDev * controllerPropGain;
            log.info(String.format("Setting the service-level controller gain for item %s to %.5f",
                    item, itemGain));
            this.itemPropGain.put(item, itemGain);
        }

    }

    @Override
    public void updateSurplus() {
        for ( Item item : this.machine ) {
            this.latestSurplusStats.get(item).addPoint(this.clock.getTime(), item.getSurplus());
        }
    }

    @Override
    public void noteNewSetup(final Item item) {

        this.changeoversSinceLatestControl.merge(item, 1, Integer::sum);

        if ( this.changeoversSinceLatestControl.get(item) >= this.changeoversPerControlCycle ) {

            // Take the new estimate of service level
            StreamSurplusStatisticsCalculator stats = this.latestSurplusStats.get(item);
            double oldServiceLevel = this.learnedServiceLevel.get(item);
            double cycleServiceLevel = stats.getServiceLevel();
            double currentLearningRate = this.learningRate.get(item);
            double newServiceLevel = ( 1 - currentLearningRate ) * oldServiceLevel + currentLearningRate * cycleServiceLevel;
            this.learnedServiceLevel.put(item, newServiceLevel);

            // Update and reset data
            this.latestSurplusStats.put(item, new StreamSurplusStatisticsCalculator());
            this.changeoversSinceLatestControl.put(item, 0);
            this.learningRate.put(item, this.learningRateDecayFactor * currentLearningRate);

            if ( log.isTraceEnabled() ) {
                log.trace(String.format("Updating the controller for item %s."
                        + " Old service level %.5f, cycle service level %.5f, new %.5f. Learning rate is now %.5f.",
                        item, oldServiceLevel, cycleServiceLevel, newServiceLevel, this.learningRate.get(item)));
            }

        }
    }

    /* (non-Javadoc)
     * @see policies.IServiceLevelController#getControl(system.Item)
     */
    @Override
    public double getControl(final Item item) {
        double error = this.targetServiceLevel.get(item) - this.learnedServiceLevel.get(item);
        double control = this.itemPropGain.get(item) * error;
        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Service level controller for item %s is %.5f due to an error of %.5f",
                    item, control, error));
        }
        return control;
    }

    /* (non-Javadoc)
     * @see policies.IServiceLevelController#getLearnedServiceLevel(system.Item)
     */
    @Override
    public Double getLearnedServiceLevel(final Item item) {
        return this.learnedServiceLevel.get(item);
    }

    /* (non-Javadoc)
     * @see policies.IServiceLevelController#getLearningRate(system.Item)
     */
    @Override
    public Double getLearningRate(final Item item) {
        return this.learningRate.get(item);
    }

}
