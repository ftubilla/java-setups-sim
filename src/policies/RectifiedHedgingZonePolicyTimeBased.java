package policies;

import java.util.Map;
import java.util.Optional;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Maps;
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
 * @author ftubilla
 *
 */
@CommonsLog
public class RectifiedHedgingZonePolicyTimeBased extends GeneralizedHedgingZonePolicyV2 {

    private static final double MU_FACTOR_TOLERANCE = 1e-3;
    private static final double TIME_TOLERANCE = 1e-5;

    /**
     * Defined as (eff * mu - d) / (eff * mubar - d)
     */
    @VisibleForTesting protected Map<Item, Double> muFactors;
    @VisibleForTesting protected Map<Item, Double> nominalTargetShift;
    // Note that these variables are defined at the point at which the changeover into the item has been completed
    protected TimeInstant currentSetupRunTime;
    protected TimeInstant currentSetupStartTime;
    protected Double currentSetupRunStartSurplus;

    @Override
    public void setUpPolicy(final Sim sim) {
        super.setUpPolicy(sim);
        this.muFactors = computeMuFactors(sim.getMachine());
        this.nominalTargetShift = computeNominalTargetShift(sim.getMachine(), this.muFactors, this.hedgingZoneSize);
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
    protected void noteNewSetup() {
        super.noteNewSetup();
        this.currentSetupStartTime = this.clock.getTime();
        this.currentSetupRunStartSurplus = this.currentSetup.getSurplus();
        this.currentSetupRunTime = computeCurrentSetupRunTime(getTarget(this.currentSetup), this.machine);
    }

    @Override
    protected double currentSetupMinTimeToTarget(Machine machine) {
        if ( isSurplusControlled(this.currentSetup) ) {
            double currentTarget = getTargetWithGivenSurplus(this.currentSetup, this.currentSetupRunStartSurplus);
            double timeToReachCurrentTarget = this.currentSetup.getFluidTimeToSurplusLevel(currentTarget);
            return timeToReachCurrentTarget;
        } else {
            TimeInstant runtimeSoFar = this.clock.getTime().subtract(this.currentSetupStartTime);
            TimeInstant remaining = this.currentSetupRunTime.subtract(runtimeSoFar);
            return Math.max(remaining.doubleValue(), TIME_TOLERANCE);
        }
    }

    @Override
    protected boolean isTimeToChangeOver() {
        if ( isSurplusControlled(this.currentSetup) ) {
            // Surplus-based stop condition
            return this.currentSetup.getSurplus() >= getTargetWithGivenSurplus(this.currentSetup, this.currentSetupRunStartSurplus)
                    - Sim.SURPLUS_TOLERANCE;
        } else {
            // Run-time based stop condition
            return this.clock.hasReachedEpoch( this.currentSetupStartTime.add(this.currentSetupRunTime) );
        }
    }

    @Override
    protected double getTarget(final Item item) {
        return getTargetWithGivenSurplus(item, item.getSurplus());
    }

    @VisibleForTesting
    protected double getTargetWithGivenSurplus(final Item item, final double surplusForTargetCalculation) {
        double surplusDev = item.getSurplusTarget() - surplusForTargetCalculation;
        double zU = ( surplusDev + this.nominalTargetShift.get(item) ) * this.muFactors.get(item) + surplusForTargetCalculation;
        if (log.isTraceEnabled()) {
            log.trace(String.format("Target for item %s is %.5f", item, zU));
        }
        return zU + this.getServiceLevelController().getControl(item);
    }

    @VisibleForTesting
    protected boolean isSurplusControlled(final Item item) {
        return this.muFactors.get(item) >= 1.0 - MU_FACTOR_TOLERANCE; 
    }

    public static TimeInstant computeCurrentSetupRunTime(final double currentSetupTarget, final Machine machine) {
        // Calculate the (e-compensated) time to reach the target
        Item currentSetup = machine.getSetup();
        double surplusChangeNeeded = currentSetupTarget - currentSetup.getSurplus();
        double runTime = surplusChangeNeeded / ( machine.getEfficiency() *
                currentSetup.getProductionRate() - currentSetup.getDemandRate());
        if ( log.isTraceEnabled() ) {
            log.trace(String.format("Starting run of %s; expected run time %.5f, surplus change needed %.5f",
                machine.getSetup(), runTime, surplusChangeNeeded));
        }
        return TimeInstant.at(runTime);
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
            double factor = ( machine.getEfficiency() * item.getProductionRate() - item.getDemandRate() ) / 
                            ( machine.getEfficiency() * muBar - item.getDemandRate() );
            log.debug(String.format("mu bar for %s is %.5f and its factor %.5f", item, muBar, factor));
            factors.put(item, factor);
        }
        return factors;
    }

    @VisibleForTesting
    protected static Map<Item, Double> computeNominalTargetShift(final Machine machine, final Map<Item, Double> muFactors,
            final Map<Item, Double> hedgingZoneSize) {
        Map<Item, Double> nominalTargetShift = Maps.newHashMap();
        for ( Item item : machine ) {
            // The goal is to shift the surplus target by some value such
            // DZ / (mu - d) = ( ZU + shift - x(0) ) / ( corrected_mu - d )
            nominalTargetShift.put(item, hedgingZoneSize.get(item) / muFactors.get(item) + item.getSurplus() - item.getSurplusTarget() );
            log.debug(String.format("Nominal ZU shift for %s is %.4f", item, nominalTargetShift.get(item)));
        }
        return nominalTargetShift;
    }
    
}
