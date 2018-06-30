package policies;

import static org.junit.Assert.*;
import static util.UtilMethods.c;

import org.junit.Before;

import params.Params;
import params.Params.ParamsBuilder;
import sim.Sim;

public class ClearTheLargestDeviationCostPolicyTest extends AbstractPolicyTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        policy = new ClearTheLargestDeviationCostPolicy();
    }

    @Override
    public void testNextItem() {

        // Item 2 has the largest backlog cost and item 0 (the current setup) is
        // at its target
        ParamsBuilder paramsBuilder = Params.builderWithDefaults();
        paramsBuilder
            .numItems(3)
            .surplusTargets(c(0.0, 0.0, 0.0))
            .initialDemand(c(0, 20, 10))
            .backlogCosts(c(1, 3, 7))
            .inventoryHoldingCosts(c(1, 100, 1))
            .productionRates(c(2, 4, 10))
            .demandRates(c(1, 1, 1))
            .setupTimes(c(1, 1, 1));

        Params params = paramsBuilder.build();
        Sim sim = getSim(params);
        policy.setUpPolicy(sim);
        policy.currentSetup = sim.getMachine().getItemById(0);
        assertEquals("Item 2 has the largest backlog cost and should be the next setup", 2, policy.nextItem().getId());

        // All items have the same backlog costs, change to something different
        // than the current setup and break ties by id
        paramsBuilder.backlogCosts(c(0, 2, 1));
        paramsBuilder.initialDemand(c(0, 5, 10));
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
