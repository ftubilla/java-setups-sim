package policies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.UtilMethods.c;

import org.junit.Before;

import params.Params;
import sim.Sim;

public class LanAndOlsenPolicyTest extends AbstractPolicyTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        policy = new LanAndOlsenPolicy();
    }

    @Override
    public void testNextItem() {

        // Item 2 has the largest deviation ratio and the current setup (item 0)
        // is at its target
        // Note that all items have the same value of Deviation + S*d
        Params params = Params.builder()
                .numItems(3)
                .surplusTargets(c(0.0, 0.0, 0.0))
                .initialDemand(c(0.0, 20.0, 20.0))
                .setupTimes(c(215.0, 15.0, 15.0))
                .demandRates(c(0.1, 0.1, 0.1))
                .build();

        // Case I: All items have the same deviation ratio, so ties broken by ID
        Sim sim = getSim(params);
        policy.setUpPolicy(sim);
        policy.currentSetup = sim.getMachine().getItemById(0);
        System.out.println(sim.getSurplusCostLowerBound().getIdealSurplusDeviation(0) + "," +
                sim.getSurplusCostLowerBound().getIdealSurplusDeviation(1) + "," +
                sim.getSurplusCostLowerBound().getIdealSurplusDeviation(2));
        assertEquals("Items 1 and 2 have the same deviation ratio, we should prefer the lowest ID", 1,
                policy.nextItem().getId());

        // Case II: Item 2 has a larger holding cost, thus a lower ideal deviation y* and a larger deviation ratio
        params = params.toBuilder()
                    .inventoryHoldingCosts(c(1, 1, 4))
                    .build();
        sim = getSim(params);
        policy.setUpPolicy(sim);
        assertEquals("Since item 2 has a larger dev. ratio, we should produce it next", 2, policy.nextItem().getId());

    }

    @Override
    public void testIsTargetBased() {
        assertTrue(policy.isTargetBased());
    }

}
