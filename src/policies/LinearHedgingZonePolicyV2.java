package policies;

import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;

import lombok.extern.apachecommons.CommonsLog;
import sim.Sim;
import system.Item;
import system.Machine;
import system.MachineSnapshot;

/**
 * A generalized version of the HZP where the target of item <tt>i</tt> depends on the surplus level at the
 * <i>start</i> of the run of this item and it is computed in a way that it attempts to minimize the difference
 * in the terms <tt>c_mu_i / rho_i</tt>.
 * 
 * @author ftubilla
 *
 */
@Deprecated
@CommonsLog
public class LinearHedgingZonePolicyV2 extends GeneralizedHedgingZonePolicy {

    private Map<Item, Double> upperHedgingZoneFactors;
    private MachineSnapshot startOfRunSnapshot;

    @Override
    public void setUpPolicy(final Sim sim ) {
        super.setUpPolicy(sim);
        this.upperHedgingZoneFactors = computeHedgingPointFactors( sim.getMachine() );
        this.startOfRunSnapshot = this.machine.getSnapshot();
    }

    @Override
    public Item nextItem() {
        Item nextItem = super.nextItem();
        log.debug(String.format("Resetting start-of-run snapshot in preparation for producing %s", nextItem));
        this.startOfRunSnapshot = null;
        return nextItem;
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

        double factor = this.upperHedgingZoneFactors.get(item);
        if ( this.startOfRunSnapshot == null ) {
            log.debug("Getting a new machine snapshot tp compute the hedging points");
            this.startOfRunSnapshot = this.machine.getSnapshot();
        }
        double projectedDeviation;
        if ( this.currentSetup.equals(item) ) {
            projectedDeviation = this.startOfRunSnapshot.getSurplusDeviation(item);
        } else {
            projectedDeviation = item.getSurplusDeviation();
        }

        double increment = projectedDeviation * factor;
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
