package policies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static util.UtilMethods.c;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import output.Recorders;
import params.Params;
import params.Params.ParamsBuilder;
import params.PolicyParams;
import params.PolicyParams.PolicyParamsBuilder;
import policies.tuning.UserDefinedLowerHedgingPointsComputationMethod;
import sim.Sim;
import sim.SimSetup;

public class HedgingZonePolicyTest extends AbstractPolicyTest {

    private HedgingZonePolicy hzpPolicy;
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.hzpPolicy = new HedgingZonePolicy();
        this.policy = this.hzpPolicy;
    }

    @Test
    public void testNextItem() {

        ParamsBuilder paramsBuilder = Params.builder();
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

        // Items 1 and 2 are outside the hedging zone; item 2 has a larger y/DZ ratio but
        // a smaller cmu coefficient. We should choose 1
        policyParamsBuilder.userDefinedLowerHedgingPoints(Optional.of(c(-5.0, -15.0, -10.0)))
                           .lowerHedgingPointsComputationMethod(UserDefinedLowerHedgingPointsComputationMethod.class.getSimpleName())
                           .name("HedgingZonePolicy");
        paramsBuilder.policyParams(policyParamsBuilder.build());
        Sim sim1 = getSim(paramsBuilder.build());
        policy.setUpPolicy(sim1);
        policy.currentSetup = sim1.getMachine().getItemById(0);
        assertEquals("The next item should be item 1 because it has the largest cmu coefficient", 1,
                policy.nextItem().getId());

        // Now give the same cmu coefficient to the items; item 2 should be selected because it has
        // a larger ratio of y / DZ
        paramsBuilder.backlogCosts(c(1.0, 3.0, 2.0));
        paramsBuilder.productionRates(c(2.0, 2.0, 3.0));
        Sim sim2 = getSim(paramsBuilder.build());
        policy.setUpPolicy(sim2);
        policy.currentSetup = sim2.getMachine().getItemById(0);
        assertEquals("The next item should be item 2 because it has the largest y/DZ ratio", 2,
                policy.nextItem().getId());

        // Now put items 1 and 2 inside the hedging zone; we will choose based
        // on the largest ratio of y/DZ
        paramsBuilder.initialDemand(c(0.0, 5.0, 5.0));
        Sim sim3 = getSim(paramsBuilder.build());
        policy.setUpPolicy(sim3);
        policy.currentSetup = sim3.getMachine().getItemById(0);
        assertEquals(
                "The next item should be item 2 because both are inside the hedging zone but 2 has larger y/DZ ratio",
                2, policy.nextItem().getId());

        // Now give items 1 and 2 the same y/DZ ratio. We should switch to 1
        // because that's the next one
        policyParamsBuilder.userDefinedLowerHedgingPoints(Optional.of(c(-5.0, -15.0, -15.0)));
        paramsBuilder.policyParams(policyParamsBuilder.build());
        Sim sim4 = getSim(paramsBuilder.build());
        policy.setUpPolicy(sim4);
        policy.currentSetup = sim4.getMachine().getItemById(0);
        assertTrue("Items 1 and 2 are tied. Next item could be 1 or 2", 
                policy.nextItem().getId()  == 1 ||
                    policy.nextItem().getId() == 2 );

    }

    @Test
    public void testIsTargetBased() {
        assertTrue("The HZP is target based", policy.isTargetBased());
    }
    
    @Test
    public void testTimeToExitFractionalHedgingZone() {

        PolicyParamsBuilder policyParamsBuilder = PolicyParams.builderWithDefaults();
        policyParamsBuilder.name(this.policy.getClass().getSimpleName())
                           .lowerHedgingPointsComputationMethod(UserDefinedLowerHedgingPointsComputationMethod.class.getSimpleName())
                           .userDefinedLowerHedgingPoints(Optional.of(c(-10, -20, -30)));

        ParamsBuilder paramsBuilder = Params.builder();
        paramsBuilder
            .numItems(3)
            .surplusTargets(c(0.0, 0.0, 0.0))
            .initialDemand(c(0.0, 20.0, 30.0))
            .backlogCosts(c(1.0, 2.0, 3.0))
            .inventoryHoldingCosts(c(1.0, 2.0, 3.0))
            .productionRates(c(2.0, 4.0, 5.0))
            .demandRates(c(0.1, 0.1, 0.1))
            .setupTimes(c(1,1,1));
        
        double tol = 1e-5;

        // No cruising tests
        policyParamsBuilder.userDefinedIsCruising(Optional.of(false));
        paramsBuilder.policyParams(policyParamsBuilder.build());

        // First start with all the surplus at the target. The time to exit should be 0
        paramsBuilder.initialDemand(c(0.0, 0.0, 0.0));
        Sim sim = new Sim(paramsBuilder.build());
        SimSetup.setUp(sim, new Recorders());
        policy.setUpPolicy(sim);
        assertEquals( 0.0, hzpPolicy.computeTimeToExitFractionalHedgingZone(0.0), tol );

        // Now put the demand on the middle of the DZ
        paramsBuilder.initialDemand(c(5, 10, 15));
        sim = new Sim(paramsBuilder.build());
        SimSetup.setUp(sim, new Recorders());
        policy.setUpPolicy(sim);
        assertEquals( 50, hzpPolicy.computeTimeToExitFractionalHedgingZone(1.0), tol );
        assertEquals( 30, hzpPolicy.computeTimeToExitFractionalHedgingZone(0.8), tol );

        // TODO Cruising tests
    }

    @Test
    public void testInsideTheFractionalHedgingZone() {
        
        PolicyParamsBuilder policyParamsBuilder = PolicyParams.builderWithDefaults();
        policyParamsBuilder.name(this.policy.getClass().getSimpleName())
                           .lowerHedgingPointsComputationMethod(UserDefinedLowerHedgingPointsComputationMethod.class.getSimpleName())
                           .userDefinedLowerHedgingPoints(Optional.of(c(-10, -20, -30)));
        
        ParamsBuilder paramsBuilder = Params.builder();
        paramsBuilder
            .numItems(3)
            .surplusTargets(c(0.0, 0.0, 0.0))
            .backlogCosts(c(1.0, 2.0, 3.0))
            .inventoryHoldingCosts(c(1.0, 2.0, 3.0))
            .productionRates(c(2.0, 4.0, 5.0))
            .demandRates(c(0.1, 0.1, 0.1))
            .setupTimes(c(1,1,1))
            .policyParams(policyParamsBuilder.build());
        
        // First put all items inside the hedging zone
        paramsBuilder.initialDemand(c(0.0, 10.0, 15.0));
        Sim sim1 = getSim(paramsBuilder.build());
        policy.setUpPolicy(sim1);
        assertTrue( hzpPolicy.isInTheFractionalHedgingZone(1.0) );
        assertTrue( hzpPolicy.isInTheFractionalHedgingZone(0.5) );
        assertFalse( hzpPolicy.isInTheFractionalHedgingZone(0.4) );
        
        // Now let's take only one item out of the hedging zone
        paramsBuilder.initialDemand(c(0.0, 21.0, 15));
        Sim sim2 = getSim(paramsBuilder.build());
        policy.setUpPolicy(sim2);
        assertTrue( hzpPolicy.isInTheFractionalHedgingZone(1.0, sim2.getMachine().getItemById(0)) );
        assertFalse( hzpPolicy.isInTheFractionalHedgingZone(1.0, sim2.getMachine().getItemById(1)) );
        assertTrue( hzpPolicy.isInTheFractionalHedgingZone(1.0, sim2.getMachine().getItemById(2)) );
        assertFalse( hzpPolicy.isInTheFractionalHedgingZone(1.0) );
    }
    
}
