package policies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.UtilMethods.c;

import org.junit.Before;

import params.Params;
import params.Params.ParamsBuilder;
import sim.Sim;

public class ClearTheLargestDeviationPolicyTest extends AbstractPolicyTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        policy = new ClearTheLargestDeviationPolicy();
    }

    @Override
    public void testNextItem() {

        // Item 2 has the largest backlog and the current setup (item 0) is at
        // its target
        ParamsBuilder paramsBuilder = Params.builderWithDefaults();
        paramsBuilder
            .numItems(3)
            .surplusTargets(c(0.0, 0.0, 0.0))
            .initialDemand(c(0.0, 20.0, 21.0))
            .backlogCosts(c(1.0, 1.0, 1.0))
            .inventoryHoldingCosts(c(1.0, 1.0, 1.0))
            .productionRates(c(10.0, 10.0, 10.0))
            .demandRates(c(1, 1, 1))
            .setupTimes(c(1, 1, 1));

        Params params = paramsBuilder.build();
        Sim sim = getSim(params);
        policy.setUpPolicy(sim);
        policy.currentSetup = sim.getMachine().getItemById(0);
        assertEquals("Item 2 has the largest backlog and should be the next setup", 2, policy.nextItem().getId());

        // All items have the same backlog, change to something different than
        // the current setup and break ties by id
        paramsBuilder.initialDemand(c(0, 0, 0));
        params = paramsBuilder.build();
        policy.setUpPolicy(getSim(params));
        assertEquals("The next item should not be the current setup and ties should be broken by ID!", 1,
                policy.nextItem().getId());

    }

    @Override
    public void testIsTargetBased() {
        assertTrue(policy.isTargetBased());
    }

}
