package policies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.UtilMethods.c;

import org.junit.Before;
import org.junit.Test;

import params.Params;
import sim.Sim;

public class RoundRobinPolicyTest extends AbstractPolicyTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        policy = new RoundRobinPolicy();
    }

    @Override
    @Test
    public void testIsTargetBased() {
        assertTrue("Round Robin is target based", policy.isTargetBased());
    }

    @Override
    @Test
    public void testNextItem() {

        Params params = Params.builder()
                .numItems(3)
                .initialSetup(0)
                .demandRates(c(0.1, 0.1, 0.1))
                .productionRates(c(1, 1, 1))
                .setupTimes(c(1, 1, 1))
                .surplusTargets(c(10, 10, 10))
                .initialDemand(c(0, 0, 0))
                .inventoryHoldingCosts(c(1, 1, 1))
                .backlogCosts(c(1, 1, 1))
                .build();

        Sim sim = getSim(params);
        policy.setUpPolicy(sim);
        policy.currentSetup = sim.getMachine().getItemById(0);
        assertTrue("The system is not ready for a new changeover, return null", policy.nextItem() == null);

        // Make the system ready for changeover
        params = params.toBuilder().surplusTargets(c(-10, -10, -10)).build();
        sim = getSim(params);
        policy.setUpPolicy(sim);
        assertEquals("The system is ready for a changeover, return the next item", policy.nextItem().getId(), 1);

        // Subsequent calls will return the same item
        assertEquals("The call to next Item is idempotent", policy.nextItem().getId(), 1);

        // Change the setup
        policy.currentSetup = sim.getMachine().getItemById(1);
        assertEquals("The system is ready for a changeover, return the next item", policy.nextItem().getId(), 2);

        policy.currentSetup = sim.getMachine().getItemById(2);
        assertEquals("The system is ready for a changeover, return the next item", policy.nextItem().getId(), 0);

    }

}


