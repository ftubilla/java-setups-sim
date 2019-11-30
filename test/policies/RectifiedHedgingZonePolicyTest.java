package policies;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static util.UtilMethods.c;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import params.Params;
import params.Params.ParamsBuilder;
import sim.Sim;
import system.Item;

@Ignore
@Deprecated
public class RectifiedHedgingZonePolicyTest extends AbstractPolicyTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.policy = new RectifiedHedgingZonePolicy();
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
        RectifiedHedgingZonePolicy rhzp = (RectifiedHedgingZonePolicy) this.policy;
        for ( Item item : sim.getMachine() ) {
            double yStar = sim.getSurplusCostLowerBound().getIdealSurplusDeviation(item.getId());
            double muFactor = rhzp.muFactors.get(item);
            assertEquals( ( yStar - item.getDemandRate() * item.getSetupTime() ) / muFactor + item.getSurplus() - item.getSurplusTarget(), 
                    rhzp.nominalTargetShift.get(item), 1e-4 );
        }

    }

    @Test
    public void testIsHighPriority() {
        ParamsBuilder paramsBuilder = Params.builder()
                .numItems(3)
                .backlogCosts(c(10, 20, 30))
                .inventoryHoldingCosts(c(10, 20, 30))
                .demandRates(c(0.1, 0.1, 0.1))
                .productionRates(c(3, 1, 1))
                .setupTimes(c(1, 1, 1));
        Sim sim = getSim(paramsBuilder.build());
        this.policy.setUpPolicy(sim);
        RectifiedHedgingZonePolicy rhzp = (RectifiedHedgingZonePolicy) this.policy;
        assertTrue( rhzp.isHighPriority(sim.getMachine().getItemById(0)) );
        assertFalse( rhzp.isHighPriority(sim.getMachine().getItemById(1)) );
        assertTrue( rhzp.isHighPriority(sim.getMachine().getItemById(2)) );
    }

    @Test
    public void testGetTarget() {
        double tol = 1e-4;
        ParamsBuilder paramsBuilder = Params.builder()
                .numItems(3)
                .surplusTargets(c(0, 0, 0))
                .initialDemand(c(0,0, 0))
                .backlogCosts(c(10, 20, 15))
                .inventoryHoldingCosts(c(1, 2, 1.5))
                .demandRates(c(1, 1, 1))
                .productionRates(c(30, 15, 10)) // Note that items 1 and 2 have the highest priority
                .meanTimeToFail(120)
                .meanTimeToRepair(10)
                .setupTimes(c(1, 1, 1));
        Sim sim = getSim(paramsBuilder.build());
        this.policy.setUpPolicy(sim);
        RectifiedHedgingZonePolicy rhzp = (RectifiedHedgingZonePolicy) this.policy;
        rhzp.currentSetup = sim.getMachine().getItemById(0);
        for ( Item item : sim.getMachine() ) {
            double expectedCorrectedTarget = rhzp.nominalTargetShift.get(item) * rhzp.muFactors.get(item);
            assertEquals( expectedCorrectedTarget, rhzp.getTarget(item), tol );
        }
        // Now start the run. Ensure that the target for this high-priority item doesn't change
        advanceUntilTime(4, sim, 20);
        Item item1 = sim.getMachine().getItemById(0);
        assertTrue(item1.getSurplusDeviation() > 0);
        assertTrue(item1.getSurplus() < rhzp.nominalTargetShift.get(item1));
        assertEquals( rhzp.nominalTargetShift.get(item1) * rhzp.muFactors.get(item1), rhzp.getTarget(item1), tol );
        // On the other hand, the target of item 2 now is computed using the non-zero surplus
        Item item2 = sim.getMachine().getItemById(1);
        assertEquals( ( rhzp.nominalTargetShift.get(item2) - item2.getSurplus() ) * rhzp.muFactors.get(item2) + item2.getSurplus(),
                rhzp.getTarget(item2), tol);
        // Similarly for item 3
        Item item3 = sim.getMachine().getItemById(2);
        assertEquals( ( rhzp.nominalTargetShift.get(item3) - item3.getSurplus() ) * rhzp.muFactors.get(item3) + item3.getSurplus(),
                rhzp.getTarget(item3), tol);
    }

    @Override
    public void testIsTargetBased() {
        assertFalse( this.policy.isTargetBased() );
    }

    @Override
    public void testNextItem() {
        // Skipping this test since the method is handled by the tested class' parent.
    }

}
