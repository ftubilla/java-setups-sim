package policies;

import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;

import lombok.extern.apachecommons.CommonsLog;
import sim.Sim;
import system.Item;
import system.Machine;

/**
 * This version of the policy computes the target upper hedging point at the
 * start of each run (and locks it until complete) based on the difference
 * between the items cmu ratio and the largest cmu ratio of the items. In this
 * way, the highest priority item always gets a clearing run, while lower
 * priority items get shorter runs. To prevent instability, a min-run length is
 * also enforced for items that have very low cmu ratios. This minimum run
 * length is adjusted dynamically to ensure that all items remain marginally
 * stable.
 * 
 * See notebook, December 2018.
 * 
 * @author ftubilla
 *
 */
@CommonsLog
public class LinearHedgingZonePolicyV5 extends GeneralizedHedgingZonePolicy {

    // Parameters on the min-run length controller
    public static final double PROP_GAIN = 0.15;
    public static final double LEARN_RATE = 0.15;

    /**
     * Defined as (mu - d) / (mubar - d)
     */
    private Map<Item, Double> muFactor;
    private Double currentSetupTarget;
    private Map<Item, Double> startOfRunLearnedSurplus;
    private Map<Item, Double> minRunLengthLearned;

    @Override
    public void setUpPolicy(final Sim sim) {
        super.setUpPolicy(sim);
        this.muFactor = computeMuFactors(sim.getMachine());
        this.startOfRunLearnedSurplus = Maps.newHashMap();
        // Initialize the min run lengths by first computing the system slack
        // given by 1 - rho / eff
        this.minRunLengthLearned = Maps.newHashMap();
        double slack = 1.0;
        for ( Item item : this.machine ) {
            slack = slack - item.getUtilization() / this.machine.getEfficiency();
        }
        // The initial min run length assumes that all the slack is available for the item.
        for ( Item item : this.machine ) {
            double minRunLengthItem = item.getDemandRate() * item.getSetupTime() / slack;
            log.debug(String.format("Initializing the min-run length of %s to %.5f",
                    item, minRunLengthItem));
            this.minRunLengthLearned.put(item, minRunLengthItem);
        }
    }

    @Override
    protected boolean currentSetupOnOrAboveTarget(Machine machine) {
        if ( this.currentSetupTarget == null ) {
            // This is the first time we check the target for the current setup. Compute and lock.
            lockTarget();
        }
        boolean aboveTarget = machine.getSetup().getSurplus() >= this.currentSetupTarget - Sim.SURPLUS_TOLERANCE;
        if ( aboveTarget ) {
            log.debug(String.format("Current setup %s above target %.3f", machine.getSetup(), this.currentSetupTarget));
            for ( Item item : machine ) {
                log.debug(String.format("Item %s Surplus %.3f", item, item.getSurplus()));
            }
        }
        return aboveTarget;
    }

    @Override
    protected void noteNewSetup() {
        super.noteNewSetup();
        // Change the surplus target for the new setup
        releaseLockedTarget();
        lockTarget();
    }

    @Override
    protected boolean isInTheHedgingZone(Machine machine, Item item, double deltaZ) {
        // Note that we shrink the hedging zone in proportion to the mu factor
        return getTarget(item) - item.getSurplus() <=  deltaZ * this.muFactor.get(item);
    }

    @Override
    protected double currentSetupMinTimeToTarget(Machine machine) {
        double currentTarget = getTarget(this.currentSetup);
        double timeToReachCurrentTarget = this.currentSetup.getFluidTimeToSurplusLevel(currentTarget);
        if ( log.isTraceEnabled() ) {
            String.format("It will take %.5f to reach the current target of %.5f for %s",
                    timeToReachCurrentTarget, currentTarget, this.currentSetup);
        }
        return timeToReachCurrentTarget;
    }

    @Override
    protected double getSurplusDeviation(Machine machine, Item item) {
        return getTarget(item) - item.getSurplus();
    }

    protected double getTarget(final Item item) {
        if ( this.currentSetup.equals(item) && this.currentSetupTarget != null ) {
            // The current setup has already locked the target. Return it.
            return this.currentSetupTarget;
        } else {
            double zU = item.getSurplusDeviation() * this.muFactor.get(item) + item.getSurplus();
            if ( log.isTraceEnabled() ) {
                String.format("Target for item %s is %.5f", item, zU);
            }
            return zU;
        }
    }

    protected void lockTarget() {
        if ( this.currentSetupTarget != null ) {
            throw new RuntimeException(String.format("Cannot lock the target because it is currently set to %.5f", this.currentSetupTarget));
        }
        double rawTarget = getTarget(this.currentSetup);
        updateLearnedStartOfRunSurplus(this.currentSetup);
        double minRunLength = updateMinRunLength(this.currentSetup);
        // Set the target to the greater of the raw target and the min run length, but never exceed the nominal target
        this.currentSetupTarget = Math.min( 
                Math.max(rawTarget, minRunLength + this.currentSetup.getSurplus()),
                this.currentSetup.getSurplusTarget() );
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("The min run length for current setup %s is %.5f. The raw target is %.5f. Locking"
                    + " the target to %.5f", this.currentSetup, minRunLength, rawTarget, this.currentSetupTarget));
        }
    }

    protected void releaseLockedTarget() {
        log.trace("Releasing locked target");
        this.currentSetupTarget = null;
    }

    @VisibleForTesting
    protected double updateLearnedStartOfRunSurplus(Item item) {
        double learnedSurplus = this.startOfRunLearnedSurplus.getOrDefault(item, item.getSurplus());
        double newSurplus = item.getSurplus();
        learnedSurplus = ( 1 - LEARN_RATE ) * learnedSurplus + LEARN_RATE * newSurplus;
        this.startOfRunLearnedSurplus.put(this.currentSetup, learnedSurplus);
        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Prev-start of run surplus for %s set to %.5f", this.currentSetup, learnedSurplus));
        }
        return learnedSurplus;
    }

    @VisibleForTesting
    protected static Map<Item, Double> computeMuFactors(final Machine machine) {
        Map<Item, Double> factors = Maps.newHashMap();
        double maxCmu = 0;
        for ( Item item : machine ) {
            // Find the largest cmu ratio
            double cMu = item.getCCostRate() * item.getProductionRate();
            if ( cMu > maxCmu ) {
                maxCmu = cMu;
            }
        }
        for ( Item item : machine ) {
            // Compute the adjusted mu based on how far the item is from max cmu
            double muBar = maxCmu / item.getCCostRate();
            // The factor adjusts the length of runs based on mubar
            double factor = ( item.getProductionRate() - item.getDemandRate() ) / 
                    ( muBar - item.getDemandRate() );
            log.debug(String.format("mu bar for %s is %.5f and its factor %.5f", item, muBar, factor));
            factors.put(item, factor);
        }
        return factors;
    }

    @VisibleForTesting
    protected double updateMinRunLength(Item item) {
        double startOfRunSurplus = this.startOfRunLearnedSurplus.getOrDefault(item, item.getSurplus());
        double minRunLength = this.minRunLengthLearned.get(item);
        if ( startOfRunSurplus != 0 ) {
            // If the current surplus is greater than the typical start-of-run surplus, we can lower the min
            // run length. If the surplus is instead trending down, we should increase the run length.
            double gain = 1 - PROP_GAIN * (item.getSurplus() - startOfRunSurplus) / Math.abs(startOfRunSurplus);
            if ( log.isTraceEnabled() ) {
                log.trace(String.format("Multiplying %s current min run length of %.5f by %.5f",
                        item, minRunLength, gain));
            }
            minRunLength = minRunLength * gain;
            this.minRunLengthLearned.put(this.currentSetup, minRunLength);
        }
        return minRunLength;
    }

}
