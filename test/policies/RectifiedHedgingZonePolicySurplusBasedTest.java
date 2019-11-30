package policies;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.UtilMethods.c;

import org.junit.Before;
import org.junit.Test;

import discreteEvent.Changeover;
import params.Params;
import params.Params.ParamsBuilder;
import params.PolicyParams;
import sim.Sim;
import system.Item;

public class RectifiedHedgingZonePolicySurplusBasedTest extends AbstractPolicyTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.policy = new RectifiedHedgingZonePolicySurplusBased();
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
        RectifiedHedgingZonePolicySurplusBased rhzp = (RectifiedHedgingZonePolicySurplusBased) this.policy;
        // All items should be surplus controlled
        assertTrue( rhzp.isSurplusControlled( sim.getMachine().getItemById(0)) );
        assertTrue( rhzp.isSurplusControlled( sim.getMachine().getItemById(1)) );
        assertTrue( rhzp.isSurplusControlled( sim.getMachine().getItemById(2)) );
    }

    /**
     * This is the same test as for the time based policy, but in this case all items should be surplus controlled.
     */
    @Test
    public void testCurrentSetupMinTimeToTarget() {
        double tol = 1e-4;
        PolicyParams policyParams = PolicyParams.builder()
                .name(RectifiedHedgingZonePolicySurplusBased.class.getSimpleName())
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
        RectifiedHedgingZonePolicySurplusBased rhzpx = (RectifiedHedgingZonePolicySurplusBased) sim.getPolicy();
        // For item 0, check that the run is surplus controlled
        advanceUntilBeforeEventOfType(sim, Changeover.class);
        Item item0 = sim.getMachine().getItemById(0);
        assertEquals(rhzpx.nominalTargetShift.get(item0), item0.getSurplus(), tol);
        // For item 1, check that the run is surplus controlled
        advanceOneEvent(sim);
        advanceUntilBeforeEventOfType(sim, Changeover.class);
        Item item1 = sim.getMachine().getItemById(1);
        assertEquals(rhzpx.nominalTargetShift.get(item1), item1.getSurplus(), tol);
        // For item 2, check that the run is surplus controlled controlled
        advanceNEvents(sim, 2);
        Item item2 = sim.getMachine().getItemById(2);
        advanceUntilBeforeEventOfType(sim, Changeover.class);
        assertEquals(rhzpx.getTargetWithGivenSurplus(item2, rhzpx.currentSetupRunStartSurplus), item2.getSurplus(), tol);
    }

    @Override
    public void testNextItem() {
        // Handled by parent class
    }

    @Override
    public void testIsTargetBased() {
        // Handled by parent class
    }

}
