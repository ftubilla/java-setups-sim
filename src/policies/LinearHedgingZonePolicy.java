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
 *      alpha = 1 / N sum( c_i \mu_i / \rho_i )
 * </pre>
 * Note that c_i is defined as 
 * <pre>
 *  c_i = b_i * h_i / ( b_i + h_i ).
 * </pre>
 * @author ftubilla
 *
 */
@CommonsLog
public class LinearHedgingZonePolicy extends GeneralizedHedgingZonePolicy {

    /**
     * Corresponds to (mu_i - d_i) / ( \sqrt( alpha d_i / c_i ) - d_i ) - 1
     */
    private Map<Item, Double> upperHedgingZoneFactors;

    @Override
    public void setUpPolicy(final Sim sim ) {
        super.setUpPolicy(sim);
        this.upperHedgingZoneFactors = computeHedgingPointFactors( sim.getMachine() );
    }

    @Override
    protected boolean currentSetupOnOrAboveTarget(Machine machine) {
        return machine.getSetup().getSurplus() >= getTarget(machine.getSetup()) - Sim.SURPLUS_TOLERANCE;
    }

    @Override
    protected boolean isInTheHedgingZone(Machine machine, Item item, double deltaZ) {
        return getTarget(item) - item.getSurplus() <= deltaZ;
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
        for ( Item otherItem : this.machine ) {
            if ( otherItem.equals(item) ) {
                continue;
            }
            double factor = this.upperHedgingZoneFactors.get(otherItem);
            // Project the current deviation (from the nominal target) deltaTime units forward, assuming that the deviation increases at
            // the demand rate.
            double projectedDeviation = otherItem.getSurplusDeviation() + deltaTime * otherItem.getDemandRate();
            // If this item is i and other item is j, then the equation is:
            // d_i / N-1 * (Z^U_j_nominal - x_j + d_j * delta_time) / d_j * factor_j
            increment += ( item.getDemandRate() / otherItem.getDemandRate() ) * ( 1 / (double) machine.getNumItems() - 1 ) * projectedDeviation * factor;
        }
        log.trace(String.format("The upper hedging point for %s has an increment of %.4f at %.4f time units from now", item, increment, deltaTime));
        return increment;
    }

    @VisibleForTesting
    protected static Map<Item, Double> computeHedgingPointFactors(final Machine machine) {
        Map<Item, Double> upperHedgingZoneFactors = Maps.newHashMap();
        // First compute alpha
        double alpha = 0;
        int numItems = machine.getNumItems();
        for ( Item item : machine ) {
            alpha += item.getCCostRate() * item.getProductionRate() / ( numItems * item.getUtilization() );
        }
        log.debug(String.format("Computing alpha coefficient %.3f", alpha));
        // Now determine the multiplicative factors for each item
        for ( Item item : machine ) {
            double factor = ( item.getProductionRate() - item.getDemandRate() ) / (
                    Math.sqrt( alpha * item.getDemandRate() / item.getCCostRate() ) - item.getDemandRate() ) - 1;
            log.debug(String.format("Setting Delta ZU factor for item %s to %.3f", item, factor));
            upperHedgingZoneFactors.put(item, factor);
        }
        return upperHedgingZoneFactors;
    }

}
