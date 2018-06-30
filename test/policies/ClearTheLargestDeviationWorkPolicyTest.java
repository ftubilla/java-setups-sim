package policies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.UtilMethods.c;

import org.junit.Before;

import params.Params;
import sim.Sim;

public class ClearTheLargestDeviationWorkPolicyTest extends AbstractPolicyTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        policy = new ClearTheLargestDeviationWorkPolicy();
    }

    @Override
    public void testNextItem() {

        Params params = Params.builder()
                .numItems(3)
                .surplusTargets(c(0, 0, 0))
                .initialDemand(c(0, 24, 19))
                .productionRates(c(0.1, 2.0, 1.0))
                .backlogCosts(c(1, 1, 1))
                .inventoryHoldingCosts(c(1, 1, 1))
                .build();

        Sim sim = getSim(params);
        policy.setUpPolicy(sim);
        policy.currentSetup = sim.getMachine().getItemById(0);
        assertEquals("Item 2 has the largest backlog work and should be the next setup", 2, policy.nextItem().getId());

        // All non-setup items have the same backlog work, change to something
        // different than the current setup and break ties by id
        params = params.toBuilder().initialDemand(c(0, 24, 12)).build();
        policy.setUpPolicy(getSim(params));
        assertEquals("The next item should not be the current setup and ties should be broken by ID!", 1,
                policy.nextItem().getId());

    }

    @Override
    public void testIsTargetBased() {
        assertTrue(policy.isTargetBased());
    }

}
