package policies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.UtilMethods.c;

import java.util.Optional;

import javax.management.RuntimeErrorException;

import org.junit.Before;
import org.junit.Test;

import params.Params;
import params.PolicyParams;
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
                .setupTimes(c(200.0, 10.0, 10.0))
                .demandRates(c(0.3, 0.3, 0.3))
                .build();

        // Case I: All items have the same deviation ratio, so ties broken by ID
        Sim sim = getSim(params);
        policy.setUpPolicy(sim);
        policy.currentSetup = sim.getMachine().getItemById(0);
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

    @Test
    public void testCruisingNotEnabledException() {
        Params params = Params.builder()
                .policyParams(PolicyParams.builder().userDefinedIsCruising(Optional.of(true)).build())
                .build();
        Sim sim = getSim(params);
        boolean exceptionThrown = false;
        try {
            policy.setUpPolicy(sim);
        } catch ( RuntimeException e ) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }

    @Override
    public void testIsTargetBased() {
        assertTrue(policy.isTargetBased());
    }

}
