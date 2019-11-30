package policies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static util.UtilMethods.c;

import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import discreteEvent.Changeover;
import discreteEvent.ControlEvent;
import metrics.TimeFractionsMetrics.Metric;
import params.Params;
import params.Params.ParamsBuilder;
import params.PolicyParams;
import params.PolicyParams.PolicyParamsBuilder;
import policies.tuning.HeuristicBoundBasedLowerHedgingPointsComputationMethod;
import policies.tuning.MakeToOrderBoundBasedLowerHedgingPointsComputationMethod;
import policies.tuning.UserDefinedLowerHedgingPointsComputationMethod;
import sim.Sim;
import system.Item;
import system.Machine;

public class RectifiedHedgingZonePolicyTimeBasedTest extends AbstractPolicyTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.policy = new RectifiedHedgingZonePolicyTimeBased();
    }

    @Test
    public void testLowerHedgingPointComputationMethod() {
        ParamsBuilder paramsBuilder = Params.builder();
        paramsBuilder
            .numItems(3)
            .surplusTargets(c(0.0, 0.0, 0.0))
            .initialDemand(c(0.0, 20.0, 30.0))
            .backlogCosts(c(1.0, 2.0, 3.0))
            .inventoryHoldingCosts(c(1.0, 2.0, 3.0))
            .productionRates(c(2.0, 4.0, 1.0))
            .demandRates(c(0.1, 0.1, 0.1))
            .setupTimes(c(1,1,1))
            .initialSetup(0);

        PolicyParamsBuilder policyParamsBuilder = PolicyParams.builderWithDefaults();

        // Case I: The lower hedging point method is the user defined. We should use this one.
        policyParamsBuilder.userDefinedLowerHedgingPoints(Optional.of(c(-5.0, -15.0, -10.0)))
                           .lowerHedgingPointsComputationMethod(UserDefinedLowerHedgingPointsComputationMethod.class.getSimpleName())
                           .name(RectifiedHedgingZonePolicyTimeBased.class.getSimpleName());
        PolicyParams policyParamsI = policyParamsBuilder.build();
        paramsBuilder.policyParams(policyParamsI);
        Sim simI = getSim(paramsBuilder.build());
        RectifiedHedgingZonePolicyTimeBased dynamicHZPI = (RectifiedHedgingZonePolicyTimeBased) simI.getPolicy();
        assertEquals( UserDefinedLowerHedgingPointsComputationMethod.class,
                dynamicHZPI.getLowerHedgingPointComputationMethod(policyParamsI).getClass());

        // Case II: The lower hedging point method is based on the surplus bound. We should override and use the heuristic one.
        policyParamsBuilder.userDefinedLowerHedgingPoints(Optional.of(c(-5.0, -15.0, -10.0)))
        .lowerHedgingPointsComputationMethod(MakeToOrderBoundBasedLowerHedgingPointsComputationMethod.class.getSimpleName())
        .name(RectifiedHedgingZonePolicyTimeBased.class.getSimpleName());
        PolicyParams policyParamsII = policyParamsBuilder.build();
        paramsBuilder.policyParams(policyParamsII);
        Sim simII = getSim(paramsBuilder.build());
        RectifiedHedgingZonePolicyTimeBased dynamicHZPII = (RectifiedHedgingZonePolicyTimeBased) simII.getPolicy();
        assertEquals( HeuristicBoundBasedLowerHedgingPointsComputationMethod.class,
                dynamicHZPII.getLowerHedgingPointComputationMethod(policyParamsII).getClass());

        // Case III: The given method is the heuristic one
        policyParamsBuilder.userDefinedLowerHedgingPoints(Optional.of(c(-5.0, -15.0, -10.0)))
        .lowerHedgingPointsComputationMethod(HeuristicBoundBasedLowerHedgingPointsComputationMethod.class.getSimpleName())
        .name(RectifiedHedgingZonePolicyTimeBased.class.getSimpleName());
        PolicyParams policyParamsIII = policyParamsBuilder.build();
        paramsBuilder.policyParams(policyParamsIII);
        Sim simIII = getSim(paramsBuilder.build());
        RectifiedHedgingZonePolicyTimeBased dynamicHZPIII = (RectifiedHedgingZonePolicyTimeBased) simIII.getPolicy();
        assertEquals( HeuristicBoundBasedLowerHedgingPointsComputationMethod.class,
                dynamicHZPIII.getLowerHedgingPointComputationMethod(policyParamsIII).getClass());
    }

    @Test
    public void testNominalTargetShiftFactors() {
        ParamsBuilder paramsBuilder = Params.builder()
                .numItems(2)
                .backlogCosts(c(10, 20))
                .inventoryHoldingCosts(c(3, 4))
                .demandRates(c(1, 1))
                .productionRates(c(30, 40))
                .meanTimeToFail(120)
                .meanTimeToRepair(10)
                .surplusTargets(c(5, 6))
                .setupTimes(c(1, 3));
        Sim sim = getSim(paramsBuilder.build());
        this.policy.setUpPolicy(sim);
        RectifiedHedgingZonePolicyTimeBased rhzp = (RectifiedHedgingZonePolicyTimeBased) this.policy;
        for ( Item item : sim.getMachine() ) {
            double yStar = sim.getSurplusCostLowerBound().getIdealSurplusDeviation(item.getId());
            double muFactor = rhzp.muFactors.get(item);
            assertEquals( ( yStar - item.getDemandRate() * item.getSetupTime() ) / muFactor + item.getSurplus() - item.getSurplusTarget(), 
                    rhzp.nominalTargetShift.get(item), 1e-4 );
        }
    }

    @Test
    public void testComputeMuFactors() {

        ParamsBuilder paramsBuilder = Params.builder();
        paramsBuilder
            .numItems(2)
            .backlogCosts(c(10, 1))
            .productionRates(c(30, 10))
            .inventoryHoldingCosts(c(1, 2))
            .meanTimeToFail(120)
            .meanTimeToRepair(10);

        Sim sim = getSim(paramsBuilder.build());
        Map<Item, Double> muFactors = RectifiedHedgingZonePolicyTimeBased.computeMuFactors(sim.getMachine());

        double tol = 1e-4;
        Machine machine = sim.getMachine();
        Item item1 = machine.getItemById(0);
        Item item2 = machine.getItemById(1);
        assertEquals("Item 1 has the highest cmu factor, so it should have a factor of 1",
                1, muFactors.get(item1), tol);
        double correctedMu2 = item1.getCCostRate() * item1.getProductionRate() / item2.getCCostRate();
        double machEff = machine.getEfficiency();
        double expectedMuFactor2 = ( machEff * item2.getProductionRate() - item2.getDemandRate() ) / ( machEff * correctedMu2 - item2.getDemandRate() );
        assertEquals( expectedMuFactor2, muFactors.get(item2), tol);

    }

    @Test
    public void testIsSurplusControlled() {
        ParamsBuilder paramsBuilder = Params.builder()
                .numItems(3)
                .backlogCosts(c(10, 20, 30))
                .inventoryHoldingCosts(c(10, 20, 30))
                .demandRates(c(0.1, 0.1, 0.1))
                .productionRates(c(3, 1, 1))
                .setupTimes(c(1, 1, 1));
        Sim sim = getSim(paramsBuilder.build());
        this.policy.setUpPolicy(sim);
        RectifiedHedgingZonePolicyTimeBased rhzp = (RectifiedHedgingZonePolicyTimeBased) this.policy;
        assertTrue( rhzp.isSurplusControlled( sim.getMachine().getItemById(0)) );
        assertFalse( rhzp.isSurplusControlled( sim.getMachine().getItemById(1)) );
        assertTrue( rhzp.isSurplusControlled( sim.getMachine().getItemById(2)) );
    }

    @Test
    public void testComputeCurrentSetupRunTime() {
        ParamsBuilder paramsBuilder = Params.builder()
                .numItems(2)
                .demandRates(c(1.0, 1.0))
                .productionRates(c(4.0, 4.0))
                .surplusTargets(c(10.0, 10.0))
                .meanTimeToFail(120)
                .meanTimeToRepair(10);
        Sim sim = getSim(paramsBuilder.build());
        Machine machine = sim.getMachine();
        double target = 10.0;
        Item currentSetup = machine.getSetup();
        double expectedTime = ( target - currentSetup.getSurplus() ) /
                ( machine.getEfficiency() * currentSetup.getProductionRate() - currentSetup.getDemandRate() );
        assertEquals( expectedTime, RectifiedHedgingZonePolicyTimeBased.computeCurrentSetupRunTime(target, machine).doubleValue(), 1e-4 );
    }

    @Test
    public void testGetTargetWithGivenSurplus() {

        double tol = 1e-4;

        PolicyParams policyParams = PolicyParams.builder()
                .name(RectifiedHedgingZonePolicyTimeBased.class.getSimpleName())
                .build();

        ParamsBuilder paramsBuilder = Params.builder()
                .numItems(2)
                .demandRates(c(1.0, 1.0))
                .productionRates(c(4.0, 4.0))
                .surplusTargets(c(10.0, 10.0))
                .meanTimeToFail(120)
                .meanTimeToRepair(10)
                .policyParams(policyParams);

        Sim sim = getSim(paramsBuilder.build());
        RectifiedHedgingZonePolicyTimeBased rhzpt = (RectifiedHedgingZonePolicyTimeBased) sim.getPolicy();
        Map<Item, Double> muFactors = RectifiedHedgingZonePolicyTimeBased.computeMuFactors(sim.getMachine());
        Map<Item, Double> targetShifts = RectifiedHedgingZonePolicyTimeBased.computeNominalTargetShift(sim.getMachine(), muFactors, rhzpt.hedgingZoneSize);
        // Case I: With given surplus
        Item item0 = sim.getMachine().getItemById(0);
        double givenSurplus = -20.0;
        double expectedTarget = ( item0.getSurplusTarget() + targetShifts.get(item0) - givenSurplus ) * muFactors.get(item0) + givenSurplus;
        assertEquals( expectedTarget, rhzpt.getTargetWithGivenSurplus(item0, givenSurplus), tol);
        // Case II: With no given surplus
        expectedTarget = ( item0.getSurplusTarget() + targetShifts.get(item0) - item0.getSurplus() ) * muFactors.get(item0) + item0.getSurplus();
        assertEquals( expectedTarget, rhzpt.getTarget(item0), tol);
    }

    @Test
    public void testIsTimeToChangeOver() {
        double tol = 1e-4;
        PolicyParams policyParams = PolicyParams.builder()
                .name(RectifiedHedgingZonePolicyTimeBased.class.getSimpleName())
                .build();
        ParamsBuilder paramsBuilder = Params.builder()
                .numItems(2)
                .surplusTargets(c(0, 0))
                .initialDemand(c(0, 0))
                .backlogCosts(c(10, 15))
                .inventoryHoldingCosts(c(1, 1.5))
                .demandRates(c(1, 1))
                .productionRates(c(30, 10))
                .meanTimeToFail(120)
                .meanTimeToRepair(10)
                .setupTimes(c(1, 1))
                .policyParams(policyParams);
        Sim sim = getSim(paramsBuilder.build());
        advanceUntilTime(0.01, sim, 20);
        RectifiedHedgingZonePolicyTimeBased rhzpt = (RectifiedHedgingZonePolicyTimeBased) sim.getPolicy();
        // Case I: Item 0 is surplus controlled, so the run concludes until we reach its target
        Item item0 = sim.getMachine().getSetup();
        assertTrue( rhzpt.isSurplusControlled(item0) );
        advanceUntilBeforeEventOfType(sim, Changeover.class);
        assertEquals( 0.0 + rhzpt.nominalTargetShift.get(item0), item0.getSurplus(), tol );
        // Case II: Item 1 is time controlled, so the run should conclude before we reach the target
        advanceUntilBeforeEventOfType(sim, ControlEvent.class);
        double startOfRunTime = sim.getTime().doubleValue() + sim.getMachine().getItemById(1).getSetupTime();
        advanceUntilBeforeEventOfType(sim, Changeover.class);
        double endOfRunTime = sim.getTime().doubleValue();
        assertEquals( endOfRunTime - startOfRunTime, rhzpt.currentSetupRunTime.doubleValue(), tol);
    }

    @Test
    public void testCurrentSetupMinTimeToTarget() {
        double tol = 1e-4;
        PolicyParams policyParams = PolicyParams.builder()
                .name(RectifiedHedgingZonePolicyTimeBased.class.getSimpleName())
                .build();
        Params params = Params.builder()
                .backlogCosts(c(10.0, 10.0, 0.25))
                .inventoryHoldingCosts(c(10.0, 10.0, 2.5))
                .productionRates(c(10, 10, 10))
                .initialDemand(c(100, 100, 100))
                .surplusTargets(c(0, 0, 0))
                .setupTimes(c(10, 10, 10))
                .policyParams(policyParams)
                .meanTimeToFail(1)
                .meanTimeToRepair(0.2)
                .build();
        Sim sim = getSim(params);
        RectifiedHedgingZonePolicyTimeBased rhzpt = (RectifiedHedgingZonePolicyTimeBased) sim.getPolicy();
        // For item 0, check that the run is surplus controlled
        advanceUntilBeforeEventOfType(sim, Changeover.class);
        Item item0 = sim.getMachine().getItemById(0);
        assertEquals(rhzpt.nominalTargetShift.get(item0), item0.getSurplus(), tol);
        // For item 1, check that the run is surplus controlled
        advanceOneEvent(sim);
        advanceUntilBeforeEventOfType(sim, Changeover.class);
        Item item1 = sim.getMachine().getItemById(1);
        assertEquals(rhzpt.nominalTargetShift.get(item1), item1.getSurplus(), tol);
        // For item 2, check that the run is time controlled
        advanceNEvents(sim, 2);
        double timeTarget = rhzpt.currentSetupRunTime.doubleValue();
        double currentTime = sim.getTime().doubleValue();
        Item item2 = sim.getMachine().getItemById(2);
        advanceUntilBeforeEventOfType(sim, Changeover.class);
        double repairTimeItem2 = sim.getMetrics().getTimeFractionsMetrics().getFraction(Metric.REPAIR, item2);
        double newTime = sim.getTime().doubleValue();
        assertTrue("There should be at least one failure during item's 2 run for this test", repairTimeItem2 > 0);
        assertEquals(timeTarget, newTime - currentTime, tol);
    }

    @Override
    public void testNextItem() {
        // This method is handled by the super
    }

    @Override
    public void testIsTargetBased() {
        assertFalse(this.policy.isTargetBased());
    }

}
