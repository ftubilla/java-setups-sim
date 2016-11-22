package metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import discreteEvent.Event;
import discreteEvent.EventListener;
import lombok.extern.apachecommons.CommonsLog;
import metrics.surplusstatistics.BatchSurplusStatisticsCalculator;
import metrics.surplusstatistics.SurplusStatistics;
import sim.Sim;
import system.Item;

@CommonsLog
public class AverageSurplusByServiceLevelMetrics {

    private static final int MAX_IT = 1000;

    private final Map<Item, List<Pair<Double, Double>>> surplusDataPoints;
    private final Map<Item, Double>                     originalSurplusTargets;
    private final double                                tolerance;

    public AverageSurplusByServiceLevelMetrics(Sim sim) {

        this.tolerance = sim.getParams().getConvergenceTolerance();
        surplusDataPoints = new HashMap<Item, List<Pair<Double, Double>>>();
        originalSurplusTargets = new HashMap<Item, Double>();

        for (Item item : sim.getMachine()) {
            surplusDataPoints.put(item, new ArrayList<>());
            originalSurplusTargets.put(item, sim.getParams().getSurplusTargets().get(item.getId()));
        }

        sim.getListenersCoordinator().addAfterEventListener(new EventListener() {
            @Override
            public void execute(Event event, Sim sim) {
                if (sim.isTimeToRecordData()) {
                    for (Item item : sim.getMachine()) {
                        Pair<Double, Double> dataPoint = Pair.of(sim.getTime(), item.getSurplus());
                        surplusDataPoints.get(item).add(dataPoint);
                    }
                }
            }
        });

    }

    /**
     * Finds the optimal offset of the trajectory for achieving the desired
     * level of service.
     * 
     * @param item
     * @param desiredServiceLevel
     * @return Pair<Double, SurplusStatistics> A pair of the offset and surplus
     *         statistics values found.
     */
    public Pair<Double, SurplusStatistics> findOptimalOffsetForServiceLevel(Item item, double desiredServiceLevel) {

        List<Pair<Double, Double>> dataPoints = surplusDataPoints.get(item);
        double originalTarget = originalSurplusTargets.get(item);

        // Find the current minimum surplus
        SurplusStatistics initialStats = BatchSurplusStatisticsCalculator.calculate(dataPoints);
        double minSurplus = initialStats.getMinSurplus();

        // Create the bounds for the binary search
        double[] targetBounds = { 0.0, originalTarget - minSurplus };
        double[] serviceLevels = { 0.0, 1.0 };
        SurplusStatistics currentStats = BatchSurplusStatisticsCalculator.calculate(dataPoints);
        log.trace(String.format("Starting service level item %s = %.5f with target %.5f", item.getId(), currentStats.getServiceLevel(), item.getSurplusTarget()));
        SurplusStatistics lowerBoundStats = BatchSurplusStatisticsCalculator.translateAndCalculate(-originalTarget,
                dataPoints);
        SurplusStatistics upperBoundStats = BatchSurplusStatisticsCalculator.translateAndCalculate(-minSurplus,
                dataPoints);
        SurplusStatistics[] stats = { lowerBoundStats, upperBoundStats };

        if (desiredServiceLevel == 0.0) {
            return Pair.of(-originalTarget, lowerBoundStats);
        }
        if (desiredServiceLevel == 1.0) {
            return Pair.of(-minSurplus, upperBoundStats);
        }

        double error = Double.MAX_VALUE;
        SurplusStatistics newStats = null;
        int numIt = 0;
        Double target = null;
        while (error > tolerance) {
            target = 0.5 * (targetBounds[0] + targetBounds[1]);
            newStats = BatchSurplusStatisticsCalculator.translateAndCalculate(target - originalTarget, dataPoints);
            double newServiceLevel = newStats.getServiceLevel();
            int index;
            if (newServiceLevel == desiredServiceLevel) {
                break;
            }
            if (newServiceLevel < desiredServiceLevel) {
                // New lower bound
                index = 0;
            } else {
                // New upper bound
                index = 1;
            }
            targetBounds[index] = target;
            stats[index] = newStats;
            serviceLevels[index] = newServiceLevel;
            error = Math.abs(newServiceLevel - desiredServiceLevel) / desiredServiceLevel;
            numIt++;
            log.trace(String.format("Iteration %d error %.5f service level %.5f surplus target %.5f", numIt, error,
                    newServiceLevel, target));
            if (numIt > MAX_IT) {
                throw new RuntimeException(
                        String.format("Could not converge to service level. Current value %.6f", newServiceLevel));
            }
        }

        // Convert the best target into an offset and return together with the
        // new stats
        return Pair.of(target, newStats);
    }
}
