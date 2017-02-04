package policies;

import static util.UtilMethods.c;
import static util.UtilMethods.cint;

import java.util.Optional;

import org.junit.Test;

import params.Params;
import params.Params.ParamsBuilder;
import params.PolicyParams;
import params.PolicyParams.PolicyParamsBuilder;
import sim.Sim;

public class GallegoRecoveryPolicyTest extends AbstractPolicyTest {

    @Test
    public void testSetUp() {
        
        ParamsBuilder paramsBuilder = Params.builderWithDefaults();
        paramsBuilder
            .numItems(3)
            .surplusTargets(c(0.0, 0.0, 0.0))
            .initialDemand(c(0.0, 20.0, 30.0))
            .backlogCosts(c(1.0, 2.0, 3.0))
            .inventoryHoldingCosts(c(1.0, 2.0, 3.0))
            .productionRates(c(2.0, 4.0, 1.0))
            .demandRates(c(0.1, 0.1, 0.1))
            .setupTimes(c(1,1,1));
        
        PolicyParamsBuilder policyParamsBuilder = PolicyParams.builderWithDefaults();
        policyParamsBuilder.name("GallegoRecoveryPolicy")
                           .userDefinedProductionSequence(Optional.of(cint(0, 1, 2)));

        paramsBuilder.policyParams(policyParamsBuilder.build());

        Params params = paramsBuilder.build();
        Sim sim = getSim(params);
        GallegoRecoveryPolicy grp = new GallegoRecoveryPolicy();
        grp.setUpPolicy(sim);

    }

    @Override
    public void testNextItem() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void testIsTargetBased() {
        // TODO Auto-generated method stub
        
    }

}
