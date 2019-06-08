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
import params.Params;
import params.Params.ParamsBuilder;
import params.PolicyParams;
import params.PolicyParams.PolicyParamsBuilder;
import policies.tuning.UserDefinedLowerHedgingPointsComputationMethod;
import sim.Sim;
import sim.TimeInstant;
import system.Item;
import system.Machine;

public class DynamicHedgingZonePolicyTest extends AbstractPolicyTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.policy = new DynamicHedgingZonePolicy();
    }

    @Test
    public void testComputeMuFactors() {

        ParamsBuilder paramsBuilder = Params.builder();
        paramsBuilder
            .numItems(2)
            .backlogCosts(c(10, 1))
            .productionRates(c(30, 10))
            .inventoryHoldingCosts(c(1, 2));

        Sim sim = getSim(paramsBuilder.build());
        Map<Item, Double> muFactors = DynamicHedgingZonePolicy.computeMuFactors(sim.getMachine());

        double tol = 1e-4;
        Machine machine = sim.getMachine();
        Item item1 = machine.getItemById(0);
        Item item2 = machine.getItemById(1);
        assertEquals("Item 1 has the highest cmu factor, so it should have a factor of 1",
                1, muFactors.get(item1), tol);
        double correctedMu2 = item1.getCCostRate() * item1.getProductionRate() / item2.getCCostRate();
        double expectedMuFactor2 = ( item2.getProductionRate() - item2.getDemandRate() ) / ( correctedMu2 - item2.getDemandRate() );
        assertEquals( expectedMuFactor2, muFactors.get(item2), tol);

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
        DynamicHedgingZonePolicy dhzp = (DynamicHedgingZonePolicy) this.policy;
        for ( Item item : sim.getMachine() ) {
            double yStar = sim.getSurplusCostLowerBound().getIdealSurplusDeviation(item.getId());
            double muFactor = dhzp.muFactors.get(item);
            assertEquals( ( yStar - item.getDemandRate() * item.getSetupTime() ) / muFactor + item.getSurplus() - item.getSurplusTarget(), 
                    dhzp.nominalTargetShift.get(item), 1e-4 );
        }

    }

    @Test
    public void testGetTarget() {
        double tol = 1e-4;
        ParamsBuilder paramsBuilder = Params.builder()
                .numItems(2)
                .surplusTargets(c(0, 0))
                .initialDemand(c(0,0))
                .backlogCosts(c(10, 20))
                .inventoryHoldingCosts(c(3, 4))
                .demandRates(c(1, 1))
                .productionRates(c(30, 40))
                .meanTimeToFail(120)
                .meanTimeToRepair(10)
                .setupTimes(c(1, 3));
        Sim sim = getSim(paramsBuilder.build());
        this.policy.setUpPolicy(sim);
        DynamicHedgingZonePolicy dhzp = (DynamicHedgingZonePolicy) this.policy;
        dhzp.currentSetup = sim.getMachine().getItemById(0);
        for ( Item item : sim.getMachine() ) {
            double expectedCorrectedTarget = dhzp.nominalTargetShift.get(item) * dhzp.muFactors.get(item);
            assertEquals( expectedCorrectedTarget, dhzp.getTarget(item), tol );
        }
        // Now lock the target for item 1 and start the run. Ensure that the target for this item doesn't change
        dhzp.lockTarget();
        advanceUntilTime(4, sim, 10);
        Item item1 = sim.getMachine().getItemById(0);
        assertTrue(item1.getSurplusDeviation() > 0);
        assertTrue(item1.getSurplus() < dhzp.nominalTargetShift.get(item1));
        assertEquals( dhzp.nominalTargetShift.get(item1) * dhzp.muFactors.get(item1), dhzp.getTarget(item1), tol );
        // On the other hand, the target of item 2 now is computed using the non-zero surplus
        Item item2 = sim.getMachine().getItemById(1);
        assertEquals( ( dhzp.nominalTargetShift.get(item2) - item2.getSurplus() ) * dhzp.muFactors.get(item2) + item2.getSurplus(),
                dhzp.getTarget(item2), tol);
    }

    @Override
    public void testIsTargetBased() {
        assertFalse( this.policy.isTargetBased() );
    }

    @Test
    public void  testOverriddenMethods() {
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

        // Items 1 and 2 are outside the hedging zone; item 2 has a larger y/DZ ratio but
        // a smaller cmu coefficient.
        policyParamsBuilder.userDefinedLowerHedgingPoints(Optional.of(c(-5.0, -15.0, -10.0)))
                           .lowerHedgingPointsComputationMethod(UserDefinedLowerHedgingPointsComputationMethod.class.getSimpleName())
                           .name(DynamicHedgingZonePolicy.class.getSimpleName());
        paramsBuilder.policyParams(policyParamsBuilder.build());
        Sim sim = getSim(paramsBuilder.build());

        DynamicHedgingZonePolicy dynamicHZP = (DynamicHedgingZonePolicy) sim.getPolicy();

        // Produce up until the changeover and run the sim until the next control event
        advanceUntilBeforeEventOfType(sim, Changeover.class);

        // Test that setup 0 is at its target
        assertTrue( dynamicHZP.currentSetupOnOrAboveTarget(sim.getMachine()));
        assertTrue( sim.getMachine().getSetup().getSurplus() >= dynamicHZP.getTarget(sim.getMachine().getSetup()) );

        // Test that item 1 is in its hedging zone if its size is  large enough
        Item item1 = sim.getMachine().getItemById(1);
        double target1 = dynamicHZP.getTarget(item1);
        assertTrue(  dynamicHZP.isInTheHedgingZone(sim.getMachine(), sim.getMachine().getItemById(1), target1 - item1.getSurplus() + 1));
        assertFalse( dynamicHZP.isInTheHedgingZone(sim.getMachine(), sim.getMachine().getItemById(1), target1 - item1.getSurplus() - 1));

        // Change setup to item 1, produce, and check that the time to reach the target is correct
        double timeToReachTarget = ( target1 - item1.getSurplus() + item1.getSetupTime() * item1.getDemandRate() ) / ( item1.getProductionRate() - item1.getDemandRate() );
        TimeInstant t0 = sim.getClock().getTime();
        advanceUntilBeforeEventOfType(sim, ControlEvent.class);
        advanceUntilBeforeEventOfType(sim, Changeover.class);
        TimeInstant t1 = sim.getClock().getTime();
        assertEquals( timeToReachTarget, t1.subtract(t0).doubleValue() - item1.getSetupTime(), 1e-4 );

    }

    @Override
    public void testNextItem() {
        // Skipping this test since the method is handled by the tested class' parent.
    }

}
