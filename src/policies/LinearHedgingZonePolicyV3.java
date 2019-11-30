package policies;

import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;

import lombok.extern.apachecommons.CommonsLog;
import sim.Sim;
import system.Item;
import system.Machine;

/**
 * A generalized version of the HZP where the targets are a linear function of the surplus levels of the other items.
 * For example
 * <pre>
 *      target_i = nominal_target_i  + sum_{j != i} C_{i,j} ( nominal_target_j - surplus_j ) 
 * </pre>
 * The constant <tt>C_{i,j}</tt> is defined as
 * <pre>
 *      C_{i,j} = 1/(N-1) * d_i /d_j * [ ( mu_i - d_i ) / ( sqrt( alpha d_i / c_i ) - d_i )  - 1 ] 
 * </pre>
 * where
 * <pre>
 *      alpha = min( c_i \mu_i / \rho_i )
 * </pre>
 * Note that c_i is defined as 
 * <pre>
 *  c_i = b_i * h_i / ( b_i + h_i ).
 * </pre>
 * 
 * By defining <tt>alpha</tt> as the minimum value of the ratio, we guarantee that <tt>mu_i >= sqrt( alpha d_i / c_i)</tt>
 * and so the correction term <tt>C_{i,j}</tt> is non-negative. This helps prevent shrinkage of the hedging zone.
 * @author ftubilla
 *
 */
@Deprecated
@CommonsLog
public class LinearHedgingZonePolicyV3 extends GeneralizedHedgingZonePolicy {

    /**
     * Corresponds to d_i / ( N - 1 ) * (mu_i - d_i) / ( \sqrt( alpha d_i / c_i ) - d_i ) - 1
     */
    private Map<Item, Double> upperHedgingZoneFactors;
    /**
     * Corresponds to ( 1 + sum_{j != j} factor[i]^2 / d[j]^2 )^1/2
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
        for ( Item otherItem : this.machine ) {
            if ( otherItem.equals(item) ) {
                continue;
            }
            // Project the current deviation (from the nominal target) deltaTime units forward, assuming that the deviation increases at
            // the demand rate.
            double projectedDeviation = otherItem.getSurplusDeviation() + deltaTime * otherItem.getDemandRate();
            // If this item is i and other item is j, then the equation is:
            // (Z^U_j_nominal - x_j + d_j * delta_time) / d_j * factor_i
            increment +=  projectedDeviation * factor / otherItem.getDemandRate();
        }
        log.trace(String.format("The upper hedging point for %s has an increment of %.4f at %.4f time units from now", item, increment, deltaTime));
        return increment;
    }

    @VisibleForTesting
    protected static Map<Item, Double> computeHedgingPointFactors(final Machine machine) {

        Map<Item, Double> upperHedgingZoneFactors = Maps.newHashMap();
        // First compute alpha
        double alpha = Double.MAX_VALUE;
        for ( Item item : machine ) {
            double alphaItem = item.getCCostRate() * item.getProductionRate() / ( item.getUtilization() );
            if ( alphaItem < alpha ) {
                alpha = alphaItem;
            }
        }
        log.debug(String.format("Computing alpha coefficient %.3f", alpha));
        // Now determine the multiplicative factors for each item
        for ( Item item : machine ) {
            // First term is d[i] / ( N - 1 )
            double term1 = ( item.getDemandRate() / ( (double) machine.getNumItems() - 1 ) );
            // Second term is ( mu[i] - d[i] ) / ( (alpha d[i] / c[i])^(1/2) - d[i] ) - 1
            double term2 = ( item.getProductionRate() - item.getDemandRate() ) / (
                            Math.sqrt( alpha * item.getDemandRate() / item.getCCostRate() ) - item.getDemandRate() ) - 1;
            double factor = term1 * term2;
            log.debug(String.format("Setting Delta ZU factor for item %s to %.3f", item, factor));
            upperHedgingZoneFactors.put(item, factor);
        }
        return upperHedgingZoneFactors;
    }

    @VisibleForTesting
    protected static Map<Item, Double> computeHedgingZoneExpansionFactors(final Map<Item, Double> hedgingFactors) {
        Map<Item, Double> expansionFactors = Maps.newHashMap();
        for ( Item itemI : hedgingFactors.keySet() ) {
            double factorI = hedgingFactors.get(itemI);
            double sumSq = 0.0;
            for ( Item itemJ : hedgingFactors.keySet() ) {
                if ( itemJ != itemI ) {
                    sumSq += Math.pow(itemJ.getDemandRate(), 2);
                }
            }
            //TODO This is wrong: we are doing 1 / sumSq when in reality we need sum( 1/ sq)
            double expansionFactorI = Math.sqrt( 1 + Math.pow(factorI, 2) / sumSq );
            log.debug(String.format("Setting the DZ expansion factor for item %s to %.3f", itemI, expansionFactorI));
            expansionFactors.put(itemI, expansionFactorI);
        }
        return expansionFactors;
    }

}
