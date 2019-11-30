package policies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.UtilMethods.c;

import org.junit.Before;
import org.junit.Test;

import params.Params;
import params.PolicyParams;
import params.Params.ParamsBuilder;
import sim.Sim;
import system.Machine;

public class HZPImplGeneralizedHedgingZonePolicyV2Test extends AbstractPolicyTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.policy = new HZPImplGeneralizedHedgingZonePolicyV2();
    }

    @Test
    public void testGetTarget() {
        double tol = 1e-4;
        PolicyParams policyParams = PolicyParams.builder()
                .name(HZPImplGeneralizedHedgingZonePolicyV2.class.getSimpleName())
                .build();
        ParamsBuilder paramsBuilder = Params.builder()
                .numItems(3)
                .backlogCosts(c(10, 20, 30))
                .surplusTargets(c(10, 20, 30))
                .inventoryHoldingCosts(c(10, 20, 30))
                .demandRates(c(0.1, 0.1, 0.1))
                .productionRates(c(3, 1, 1))
                .setupTimes(c(1, 1, 1))
                .policyParams(policyParams);
        Sim sim = getSim(paramsBuilder.build());
        Machine machine = sim.getMachine();
        HZPImplGeneralizedHedgingZonePolicyV2 hzp = (HZPImplGeneralizedHedgingZonePolicyV2) sim.getPolicy();
        assertEquals( 10, hzp.getTarget(machine.getItemById(0)), tol );
        assertEquals( 20, hzp.getTarget(machine.getItemById(1)), tol );
        assertEquals( 30, hzp.getTarget(machine.getItemById(2)), tol );
    }

    @Override
    public void testNextItem() {
        // Handled by parent class
    }

    @Override
    public void testIsTargetBased() {
        assertTrue(this.policy.isTargetBased());
    }

}
