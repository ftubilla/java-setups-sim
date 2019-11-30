package policies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static util.UtilMethods.c;

import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import discreteEvent.Changeover;
import params.Params;
import params.Params.ParamsBuilder;
import params.PolicyParams;
import params.PolicyParams.PolicyParamsBuilder;
import policies.tuning.UserDefinedLowerHedgingPointsComputationMethod;
import sim.Sim;

public class HZPImplGeneralizedHedgingZonePolicyTest extends AbstractPolicyTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.policy = new HZPImplGeneralizedHedgingZonePolicy();
    }

    @Ignore("Ignoring this test because we are no longer throwing an exception")
    @Test
    public void testCruisingNotAllowed() {

        ParamsBuilder paramsBuilder = Params.builder();
        PolicyParamsBuilder policyParamsBuilder = PolicyParams.builderWithDefaults(); 
        policyParamsBuilder.userDefinedIsCruising(Optional.of(true))
                           .name(HZPImplGeneralizedHedgingZonePolicy.class.getSimpleName());
        paramsBuilder.policyParams(policyParamsBuilder.build());

        boolean exceptionThrown = false;
        try {
            getSim(paramsBuilder.build());
        } catch( RuntimeException e ) {
            exceptionThrown = true;
        }
        assertTrue("An exception should be thrown because cruising is not allowed", exceptionThrown);

        // Let's make now a system that should really cruise according to the lower bound
        paramsBuilder
                     .demandRates(ImmutableList.of(0.1, 0.001, 0.001))
                     .setupTimes(ImmutableList.of(1.0, 100.0, 100.0))
                     .backlogCosts(ImmutableList.of(100.0, 1.0, 1.0))
                     .policyParams(policyParamsBuilder.userDefinedIsCruising(Optional.of(false)).build());
        exceptionThrown = false;
        try {
            Sim sim = getSim(paramsBuilder.build());
            assertTrue("This should be a cruising system", sim.getSurplusCostLowerBound().getIsCruising());
        } catch( RuntimeException e ) {
            exceptionThrown = true;
        }
        assertTrue("An exception should be thrown because this is a cruising system", exceptionThrown);
    }

    @Override
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
                           .name(HZPImplGeneralizedHedgingZonePolicy.class.getSimpleName());
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
        // because that's the lowest index one
        policyParamsBuilder.userDefinedLowerHedgingPoints(Optional.of(c(-5.0, -15.0, -15.0)));
        paramsBuilder.policyParams(policyParamsBuilder.build());
        Sim sim4 = getSim(paramsBuilder.build());
        policy.setUpPolicy(sim4);
        policy.currentSetup = sim4.getMachine().getItemById(0);
        assertTrue("Items 1 and 2 are tied. Next item should be 1", 
                policy.nextItem().getId()  == 1);

        // Finally, tie all items (including item 0). We should ignore item 0 since it's the current setup
        policyParamsBuilder.userDefinedLowerHedgingPoints(Optional.of(c(-15.0, -15.0, -15.0)));
        paramsBuilder.policyParams(policyParamsBuilder.build());
        paramsBuilder.initialDemand(c(0.0, 0.0, 0.0));
        Sim sim5 = getSim(paramsBuilder.build());
        policy.setUpPolicy(sim5);
        policy.currentSetup = sim5.getMachine().getItemById(0);
        assertTrue("Items 0, 1, and 2 are tied. Next item should be 1",
                policy.nextItem().getId() == 1);
    }

    @Override
    public void testIsTargetBased() {
        assertTrue( this.policy.isTargetBased() );
    }

    @Test
    public void  testOverriddenMethods() {
        ParamsBuilder paramsBuilder = Params.builder();
        paramsBuilder
            .numItems(3)
            .surplusTargets(c(0.0, 0.0, 0.0))
            .initialDemand(c(0.0, 20.0, 30.0))
            .backlogCosts(c(1.0, 2.0, 3.0))
            .inventoryHoldingCosts(c(1.0, 2.0, 3.0))
            .productionRates(c(2.0, 4.0, 1.0))
            .demandRates(c(0.1, 0.1, 0.1))
            .setupTimes(c(1,1,1))
            .initialSetup(0);

        PolicyParamsBuilder policyParamsBuilder = PolicyParams.builderWithDefaults();

        // Items 1 and 2 are outside the hedging zone; item 2 has a larger y/DZ ratio but
        // a smaller cmu coefficient.
        policyParamsBuilder.userDefinedLowerHedgingPoints(Optional.of(c(-5.0, -15.0, -10.0)))
                           .lowerHedgingPointsComputationMethod(UserDefinedLowerHedgingPointsComputationMethod.class.getSimpleName())
                           .name(HZPImplGeneralizedHedgingZonePolicy.class.getSimpleName());
        paramsBuilder.policyParams(policyParamsBuilder.build());
        Sim sim = getSim(paramsBuilder.build());

        GeneralizedHedgingZonePolicy generalizedHZP = (GeneralizedHedgingZonePolicy) this.policy;
        generalizedHZP.setUpPolicy(sim);

        // Test that setup 0 is at its target
        assertTrue( generalizedHZP.currentSetupOnOrAboveTarget(sim.getMachine()));

        // Test that item 1 is in its hedging zone if its size is > 20
        assertTrue( generalizedHZP.isInTheHedgingZone(sim.getMachine(), sim.getMachine().getItemById(1), 21));
        assertFalse( generalizedHZP.isInTheHedgingZone(sim.getMachine(), sim.getMachine().getItemById(1), 19));

        // Change setup to item 1 and check the time to the target
        sim.getMachine().startChangeover(sim.getMachine().getItemById(1));
        double timeToTarget = 20 / ( 4.0 - 0.1);
        assertEquals( timeToTarget, generalizedHZP.currentSetupMinTimeToTarget(sim.getMachine()), 1e-4);

        // Test the surplus deviation computation
        assertEquals( 0, generalizedHZP.getSurplusDeviation(sim.getMachine(), sim.getMachine().getItemById(0)), 1e-5);
        assertEquals( 20, generalizedHZP.getSurplusDeviation(sim.getMachine(), sim.getMachine().getItemById(1)), 1e-5);
    }

    @Test
    public void testCurrentSetupMinTimeToTarget() {
        double tol = 1e-4;
        PolicyParams policyParams = PolicyParams.builder()
                .name(HZPImplGeneralizedHedgingZonePolicy.class.getSimpleName())
                .build();
        Params params = Params.builder()
                .backlogCosts(c(10.0, 10.0, 0.25))
                .inventoryHoldingCosts(c(10.0, 10.0, 2.5))
                .productionRates(c(10, 10, 10))
                .initialDemand(c(100, 100, 100))
                .surplusTargets(c(0, 0, 0))
                .setupTimes(c(10, 10, 10))
                .policyParams(policyParams)
                .meanTimeToFail(1)
                .meanTimeToRepair(0.2)
                .build();
        Sim sim = getSim(params);
        HZPImplGeneralizedHedgingZonePolicy hzp = (HZPImplGeneralizedHedgingZonePolicy) sim.getPolicy();
        // Run for N changeovers and check that the target is reached every time
        for ( int i = 0; i < 10; i++ ) {
            advanceUntilBeforeEventOfType(sim, Changeover.class);
            assertEquals(0.0, hzp.currentSetup.getSurplus(), tol);
            advanceOneEvent(sim);
        }
    }

}
