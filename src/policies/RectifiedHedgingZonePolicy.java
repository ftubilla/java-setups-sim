package policies;

import java.util.Map;
import java.util.Optional;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import lombok.extern.apachecommons.CommonsLog;
import params.PolicyParams;
import policies.tuning.HeuristicBoundBasedLowerHedgingPointsComputationMethod;
import policies.tuning.ILowerHedgingPointsComputationMethod;
import policies.tuning.MakeToOrderBoundBasedLowerHedgingPointsComputationMethod;
import sim.Sim;
import sim.TimeInstant;
import system.Item;
import system.Machine;

/**
 * A time-controlled version of the Dynamic Hedging Zone Policy. Note that, in the paper, we refer to this policy
 * as RHZPt.
 * 
 * Deprecated; use {@link RectifiedHedgingZonePolicyTimeBased} instead.
 * 
 * @author ftubilla
 *
 */
@Deprecated
@CommonsLog
public class RectifiedHedgingZonePolicy extends GeneralizedHedgingZonePolicy {

    private static final double MU_FACTOR_TOLERANCE = 1e-3;
    private static final double TIME_TOLERANCE = 1e-5;

    /**
     * Defined as (eff * mu - d) / (eff * mubar - d)
     */
    @VisibleForTesting protected Map<Item, Double> muFactors;
    @VisibleForTesting protected Map<Item, Double> nominalTargetShift;
    // Note that these variables are defined at the point at which the changeover into the item has been completed
    private TimeInstant currentSetupRunTime;
    private TimeInstant currentSetupStartTime;
    private Double currentSetupRunStartSurplus;

    @Override
    public void setUpPolicy(final Sim sim) {
        super.setUpPolicy(sim);
        this.muFactors = DynamicHedgingZonePolicy.computeMuFactors(sim.getMachine());
        this.nominalTargetShift = DynamicHedgingZonePolicy.computeNominalTargetShift(sim.getMachine(), this.muFactors, this.hedgingZoneSize);
    }

    @Override
    protected ILowerHedgingPointsComputationMethod getLowerHedgingPointComputationMethod(PolicyParams policyParams) {
        if ( policyParams.getLowerHedgingPointsComputationMethod()
                .equals(MakeToOrderBoundBasedLowerHedgingPointsComputationMethod.class.getSimpleName()) ) {
            log.info("Overriding the hedging point method to use the heuristic bound instead");
            return new HeuristicBoundBasedLowerHedgingPointsComputationMethod();
        } else {
            return super.getLowerHedgingPointComputationMethod(policyParams);
        }
    }

    @Override
    protected boolean currentSetupOnOrAboveTarget(Machine machine) {
        if ( isHighPriority(this.currentSetup) ) {
            // Runs for this item are target based
            return getTarget(this.currentSetup) - this.currentSetup.getSurplus() <= Sim.SURPLUS_TOLERANCE;
        } else {
            // Runs for this item are time based
            return this.clock.hasReachedEpoch( this.currentSetupStartTime.add(this.currentSetupRunTime) );
        }
    }

    @Override
    protected void noteNewSetup() {
        super.noteNewSetup();
        this.currentSetupStartTime = this.clock.getTime();
        this.currentSetupRunStartSurplus = this.currentSetup.getSurplus();
        // Calculate the (e-compensated) time to reach the target
        double surplusChangeNeeded = getTarget(this.currentSetup) - this.currentSetup.getSurplus();
        double runTime = surplusChangeNeeded / ( this.machine.getEfficiency() *
                this.currentSetup.getProductionRate() - this.currentSetup.getDemandRate());
        this.currentSetupRunTime = TimeInstant.at(runTime);
        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Starting run of item %d; expected run time %.5f, surplus change needed %.5f",
                this.currentSetup.getId(), runTime, surplusChangeNeeded));
        }
    }

    @Override
    protected boolean isInTheHedgingZone(Machine machine, Item item, double deltaZ) {
        // To maintain consistency, we consider the current item to be in the hedging zone if it has finished production
        // even though this might not be the case for items that are time-based
        // TODO It might be better to move this check to the generalized policy
        if ( item.equals(this.currentSetup) && currentSetupOnOrAboveTarget(machine) ) {
            return true;
        } else {
            return getTarget(item) - item.getSurplus() <= deltaZ;
        }
    }

    @Override
    protected double currentSetupMinTimeToTarget(Machine machine) {
        if ( isHighPriority(this.currentSetup) ) {
            double currentTarget = getTarget(this.currentSetup);
            double timeToReachCurrentTarget = this.currentSetup.getFluidTimeToSurplusLevel(currentTarget);
            // This is a target-based item, or have not calculated a run time yet
            return timeToReachCurrentTarget;
        } else {
            TimeInstant runtimeSoFar = this.clock.getTime().subtract(this.currentSetupStartTime);
            TimeInstant remaining = this.currentSetupRunTime.subtract(runtimeSoFar);
            return Math.max(remaining.doubleValue(), TIME_TOLERANCE);
        }
    }

    @Override
    protected double getSurplusDeviation(Machine machine, Item item) {
        return getTarget(item) - item.getSurplus();
    }

    protected double getTarget(final Item item) {
        double surplusForTargetCalculation;
        if ( isHighPriority(item) && this.currentSetup.equals(item) && this.currentSetupRunStartSurplus != null ) {
            // The target should be locked based on the surplus at the start of the run
            surplusForTargetCalculation = this.currentSetupRunStartSurplus;
        } else {
            // The target is not locked, use the current surplus
            surplusForTargetCalculation = item.getSurplus();
        }
        double surplusDev = item.getSurplusTarget() - surplusForTargetCalculation;
        double zU = ( surplusDev + this.nominalTargetShift.get(item) ) * this.muFactors.get(item) + surplusForTargetCalculation;
        if (log.isTraceEnabled()) {
            log.trace(String.format("Target for item %s is %.5f", item, zU));
        }
        return zU + this.getServiceLevelController().getControl(item);
    }

    @VisibleForTesting
    protected boolean isHighPriority(final Item item) {
        return this.muFactors.get(item) >= 1.0 - MU_FACTOR_TOLERANCE; 
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
        table.put("CURRENT_SETUP_START_TIME", "NA", this.currentSetupStartTime);
        table.put("CURRENT_SETUP_RUN_START_SURPLUS", String.format("%d", this.currentSetup.getId()), this.currentSetupRunStartSurplus);
        table.put("CURRENT_RUN_TIME", "NA", this.currentSetupRunTime);
        return Optional.of(table);
    }

}
