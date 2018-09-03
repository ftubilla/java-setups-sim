package policies;

import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;

import lombok.extern.apachecommons.CommonsLog;
import sim.Sim;
import system.Item;
import system.Machine;

/**
 * A generalized version of the HZP where the targets are a function of the work-in-progress (measured off of the upper
 * nominal target) of the remaining items.
 * In particular,
 * <pre>
 *      delta_target[i] = ( V[i] / (N-1) ) * mu[i] * [ (mu[i] - d[i])/(mu_bar[i] - d[i]) - 1 ] 
 * </pre>
 * where
 * <pre>
 *      mu_bar[i] = sqrt( alpha d[i] / c[i] )
 * </pre>
 * and
 * <pre>
 *      alpha = average( c_i \mu_i / \rho_i )
 * </pre>
 * V[i] is defined as
 * <pre>
 *      sum{ j != i } max( 0, ZU[j] - x[j] ) / mu[j]
 * </pre>
 * 
 * The multiplier of V[i] in the above equation is truncated to a set of bounds that are necessary for stability and
 * that guarantee that the surplus trajectory stays within the corridor.
 * 
 * @author ftubilla
 *
 */
@CommonsLog
public class LinearHedgingZonePolicyV4 extends GeneralizedHedgingZonePolicy {

    /**
     * TODO Make configurable. Determines how much relative slack to have in the inequalities for truncation
     */
    public static final double REL_SLACK_FACTOR = 1.5;

    /**
     * Corresponds to [mu[i] - d[i]]/[mu_bar[i]-d[i]] - 1 
     */
    private Map<Item, Double> upperHedgingZoneFactors;
    /**
     * Corresponds to sqrt( 1 + sum_{j != i} (factor[i] mu[i])^2 / mu[j]^2 )
     */
    private Map<Item, Double> hedgingZoneExpansionFactors;

    @Override
    public void setUpPolicy(final Sim sim ) {
        super.setUpPolicy(sim);
        this.upperHedgingZoneFactors = computeHedgingPointFactors( sim.getMachine() );
        this.hedgingZoneExpansionFactors = computeHedgingZoneExpansionFactors( this.upperHedgingZoneFactors );
    }

    @Override
    protected boolean currentSetupOnOrAboveTarget(Machine machine) {
        boolean aboveTarget = machine.getSetup().getSurplus() >= getTarget(machine.getSetup()) - Sim.SURPLUS_TOLERANCE;
        if ( aboveTarget ) {
            log.debug(String.format("Current setup %s above target %.3f", machine.getSetup(), getTarget(machine.getSetup())));
            for ( Item item : machine ) {
                log.debug(String.format("Item %s Surplus %.3f", item, item.getSurplus()));
            }
        }
        return aboveTarget;
    }

    @Override
    protected boolean isInTheHedgingZone(Machine machine, Item item, double deltaZ) {
        return getTarget(item) - item.getSurplus() <=  deltaZ * this.hedgingZoneExpansionFactors.get(item);
    }

    @Override
    protected double currentSetupMinTimeToTarget(Machine machine) {
        // We need to compute a lower bound on the time to reach the upper hedging point, which depends on the surplus level
        // of all the other items. To obtain this lower bound we do the following:
        double currentTarget = getTarget(this.currentSetup);
        double timeToReachCurrentTarget = this.currentSetup.getFluidTimeToSurplusLevel(currentTarget);
        double projectedTarget = getProjectedTarget(timeToReachCurrentTarget, this.currentSetup);
        return this.currentSetup.getFluidTimeToSurplusLevel(projectedTarget);
    }

    @Override
    protected double getSurplusDeviation(Machine machine, Item item) {
        return getTarget(item) - item.getSurplus();
    }

    /**
     * Returns the upper hedging point target for the item based on the current surplus levels for the rest of the item.
     * This target is the one actually used, instead of the nominal target that is fixed (and what we normally call the
     * upper hedging point).
     * 
     * @param item
     * @return target
     */
    protected double getTarget(final Item item) {
        return getProjectedTarget(0, item);
    }

    /**
     * Returns the projected target for the given item, assuming that the given time elapses and all other items are not
     * being produced.
     * 
     * @param deltaTime
     * @param item
     * @return projected target
     */
    protected double getProjectedTarget(final double deltaTime, final Item item) {
        double increment = this.computeHedgingPointIncrementAtTimeDelta(deltaTime, item);
        return item.getSurplusTarget() + increment;
    }

    /**
     * Computes the increment in the upper hedging point for the given item, based on the current surplus.
     * 
     * @param item
     * @return increment (or decrement if negative)
     */
    @VisibleForTesting
    protected double computeHedgingPointIncrement(final Item item) {
        return computeHedgingPointIncrementAtTimeDelta(0, item);
    }

    /**
     * Computes the increment in the upper hedging point for the given item, based on the surplus after the
     * given time increment. It is assumed that the surplus of all items except the given item decrease proportionally
     * to this time delta.
     * 
     * @param deltaTime
     * @param item
     * @return increment (or decrement if negative)
     */
    @VisibleForTesting
    protected double computeHedgingPointIncrementAtTimeDelta(final double deltaTime, final Item item) {
        if ( deltaTime < 0 ) throw new IllegalArgumentException(String.format("DeltaTime of %.3f is negative!", deltaTime));
        double increment = 0;
        double factor = this.upperHedgingZoneFactors.get(item);
        double nMinus1 = this.machine.getNumItems() - 1;
        for ( Item otherItem : this.machine ) {
            if ( otherItem.equals(item) ) {
                continue;
            }
            // Project the current deviation (from the nominal target) deltaTime units forward, assuming that the deviation increases at
            // the demand rate.
            double projectedDeviation = otherItem.getSurplusDeviation() + deltaTime * otherItem.getDemandRate();
            // If this item is i and other item is j, then the equation is:
            // factor[i] * (Z^U_j_nominal - x_j + d_j * delta_time)+ * mu[i] / mu[j]
            double vIContribution = Math.max(0, projectedDeviation) / otherItem.getProductionRate();
            increment +=  factor * vIContribution * item.getProductionRate() / nMinus1;
        }
        log.trace(String.format("The upper hedging point for %s has an increment of %.4f at %.4f time units from now", item, increment, deltaTime));
        return increment;
    }

    @VisibleForTesting
    protected static Map<Item, Double> computeHedgingPointFactors(final Machine machine) {

        Map<Item, Double> upperHedgingZoneFactors = Maps.newHashMap();
        // First compute alpha
        double alpha = 0.0;
        // TODO Should we compensate for the machine efficiency??
        double rho = 0.0;
        for ( Item item : machine ) {
            alpha += item.getCCostRate() * item.getProductionRate() / ( item.getUtilization() * machine.getNumItems() );
            rho += item.getUtilization();
        }
        log.debug(String.format("Computing alpha coefficient %.3f", alpha));
        // Now determine the multiplicative factors for each item
        double nMinus1 = machine.getNumItems() - 1;
        for ( Item item : machine ) {
            // First calculate mu_bar
            double muBar = Math.sqrt( alpha * item.getDemandRate() / item.getCCostRate() );
            // Now calculate the raw multiplicative factor
            double factor = ( item.getProductionRate() - item.getDemandRate() ) / ( muBar - item.getDemandRate() ) - 1;
            // Truncate if necessary
            // Note that the slack factor deflates the bounds so that, if the constraint becomes active, we are actually 10% (say)
            // from the real bound
            double truncatedFactor = Math.max( factor, - ( nMinus1 / REL_SLACK_FACTOR ) *
                    item.getUtilization() / ( rho - item.getUtilization() ));
            truncatedFactor = Math.min( truncatedFactor,
                    ( nMinus1 / REL_SLACK_FACTOR ) * item.getUtilization() / ( 1 - rho + item.getUtilization() ));
            truncatedFactor = Math.min( truncatedFactor,
                    ( nMinus1 / REL_SLACK_FACTOR ) * ( 1 - item.getUtilization() ) / ( rho - item.getUtilization() ) );
            log.debug(String.format("Setting Delta ZU factor for item %s to %.3f (%.3f before truncation)",
                    item, truncatedFactor, factor));
            upperHedgingZoneFactors.put(item, truncatedFactor);
        }
        return upperHedgingZoneFactors;
    }

    @VisibleForTesting
    protected static Map<Item, Double> computeHedgingZoneExpansionFactors(final Map<Item, Double> hedgingFactors) {
        Map<Item, Double> expansionFactors = Maps.newHashMap();
        double nMinus1 = hedgingFactors.size() - 1;
        for ( Item itemI : hedgingFactors.keySet() ) {
            double factorI = hedgingFactors.get(itemI);
            double sumSq = 0.0;
            for ( Item itemJ : hedgingFactors.keySet() ) {
                if ( itemJ != itemI ) {
                    sumSq += Math.pow( factorI * itemI.getProductionRate(), 2 ) /
                            Math.pow(itemJ.getProductionRate() * nMinus1, 2);
                }
            }
            double expansionFactorI = Math.sqrt( 1 + sumSq );
            log.debug(String.format("Setting the DZ expansion factor for item %s to %.3f", itemI, expansionFactorI));
            expansionFactors.put(itemI, expansionFactorI);
        }
        return expansionFactors;
    }

}
