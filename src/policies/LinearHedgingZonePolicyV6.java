package policies;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

import lombok.extern.apachecommons.CommonsLog;
import sim.Sim;
import system.Item;
import system.Machine;

/**
 * This version of the policy computes the target upper hedging point at the
 * start of each run (and locks it until complete) based on the difference
 * between the items cmu ratio and the largest cmu ratio of the items. In this
 * way, the highest priority item always gets a clearing run, while lower
 * priority items get shorter runs. Since runs could be very short initially
 * (i.e., when an item has a very low cmu ratio), the system might need to build
 * up a very large deviation before steady-state is reached. To counteract this
 * effect, the nominal target ZU is shifted upwards. This has the same effect as
 * the min run length from V5.
 * 
 * January 2019
 * 
 * @author ftubilla
 *
 */
@CommonsLog
public class LinearHedgingZonePolicyV6 extends GeneralizedHedgingZonePolicy {

    /**
     * A factor to be applied to the lower bound on min-run length in order to compute
     * the nominal target shift.
     */
    public static final double NOMINAL_TARGET_SHIFT_FACTOR = 2.0;

    /**
     * Defined as (mu - d) / (mubar - d)
     */
    private Map<Item, Double> muFactor;
    private Map<Item, Double> nominalTargetShift;
    private Double currentSetupTarget;

    @Override
    public void setUpPolicy(final Sim sim) {
        super.setUpPolicy(sim);
        this.muFactor = computeMuFactors(sim.getMachine());
        // Compute the system slack, given by 1 - rho/e
        double slack = 1.0;
        for ( Item item : this.machine ) {
            slack = slack - item.getUtilization() / this.machine.getEfficiency();
        }
        // The initial min run length assumes that all the slack is available for the corresp. item
        this.nominalTargetShift = Maps.newHashMap();
        for ( Item item : this.machine ) {
            double slackItem = 1 - item.getUtilization() / this.machine.getEfficiency();
            // Compute the minimum surplus change in a run for guaranteeing stability. This is a lower bound because
            // it assumes that all the slack is used by this item
            double minSurplusChangeItem = item.getDemandRate() * item.getSetupTime() * ( slackItem / slack - 1 );
            // Compute the nominal target shift
            double shift = NOMINAL_TARGET_SHIFT_FACTOR * minSurplusChangeItem / this.muFactor.get(item) -
                    0.5 * this.hedgingZoneSize.get(item);
            log.debug(String.format("Setting the nominal target shift for %s to %.5f", item, shift));
            this.nominalTargetShift.put(item, shift);
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
        // Note that we DO NOT alter the size of the hedging zone
        return getTarget(item) - item.getSurplus() <= deltaZ;
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

    @Override
    protected Optional<Item> selectItemFromReadySet(final Set<Item> readyItems) {
        // Note that we DO NOT alter the hedging zon esize
        Optional<Pair<Item, Double>> maximizingPairOpt = 
                readyItems.stream()
                .map( item -> Pair.of(item,
                   this.getSurplusDeviation(this.machine, item) / this.hedgingZoneSize.get(item) ) )
                .max( Comparator.comparingDouble( ( Pair<Item, Double> pair ) -> pair.getRight() ) );
        Item returnItem = null;
        if ( maximizingPairOpt.isPresent() ) {
            Pair<Item, Double> maximizingPair = maximizingPairOpt.get();
            log.trace(String.format("Item %s had the largest ratio of %.5f", maximizingPair.getLeft(), maximizingPair.getRight()));
            returnItem = maximizingPair.getLeft();
        }
        return Optional.ofNullable(returnItem);
    }
    
    protected double getTarget(final Item item) {
        if ( this.currentSetup.equals(item) && this.currentSetupTarget != null ) {
            // The current setup has already locked the target. Return it.
            return this.currentSetupTarget;
        } else {
            // Apply the shift to the nominal target (or, in this case, to the surplus based off the nominal target) and
            // then multiply by the mu factor.
            double zU = ( item.getSurplusDeviation() + this.nominalTargetShift.get(item) ) * this.muFactor.get(item) +
                    item.getSurplus();
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

        // Set the target to the greater of the raw target and the min run length, but never exceed the nominal target
        this.currentSetupTarget = Math.min(rawTarget,
                this.currentSetup.getSurplusTarget() + this.nominalTargetShift.get(this.currentSetup) );
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Locking the target of %s to %.5f", this.currentSetup, this.currentSetupTarget));
        }
    }

    protected void releaseLockedTarget() {
        log.trace("Releasing locked target");
        this.currentSetupTarget = null;
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

    @Override
    public Optional<Table<String, String, Object>> getDataToRecordBeforeControl() {
        Table<String, String, Object> table = HashBasedTable.create();
        for (Item item : this.machine) {
            String itemId = String.format("%d", item.getId());
            table.put("NOMINAL_TARGET", itemId, item.getSurplusTarget());
            table.put("TARGET_SHIFT", itemId, nominalTargetShift.get(item));
            table.put("HEDGING_ZONE_SIZE", itemId, this.hedgingZoneSize.get(item));
            table.put("MODIFIED_TARGET", itemId, getTarget(item));
            table.put("SURPLUS", itemId, item.getSurplus());
            table.put("CURRENT_SETUP", itemId, this.currentSetup.getId());
        }
        return Optional.of(table);
    }

}
