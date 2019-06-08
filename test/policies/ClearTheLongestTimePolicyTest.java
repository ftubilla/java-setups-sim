package policies;

import static org.junit.Assert.*;
import static util.UtilMethods.c;

import org.junit.Before;

import params.Params;
import params.Params.ParamsBuilder;
import sim.Sim;

public class ClearTheLongestTimePolicyTest extends AbstractPolicyTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        policy = new ClearTheLongestTimePolicy();
    }

    @Override
    public void testNextItem() {

        // Item 2 has the longest time to clear
        ParamsBuilder paramsBuilder = Params.builderWithDefaults();
        paramsBuilder
            .numItems(3)
            .surplusTargets(c(0.0, 0.0, 0.0))
            .initialDemand(c(0, 10, 10))
            .productionRates(c(2, 10, 5))
            .demandRates(c(1, 1, 1))
            .setupTimes(c(1, 1, 1));

        Params params = paramsBuilder.build();
        Sim sim = getSim(params);
        policy.setUpPolicy(sim);
        policy.currentSetup = sim.getMachine().getItemById(0);
        assertEquals("Item 2 has the longest time to clear and should be the next setup", 2, policy.nextItem().getId());

        // All items have the same time to clear (within tolerance), change to something different
        // than the current setup and break ties by id
        paramsBuilder.productionRates(c(2, 5, 5.00001));
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
