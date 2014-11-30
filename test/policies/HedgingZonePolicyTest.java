package policies;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import params.Params;
import params.PolicyParams;
import policies.tuning.CMuComparatorWithTiesById;
import policies.tuning.UserDefinedLowerHedgingPointsComputationMethod;
import sim.Sim;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class HedgingZonePolicyTest extends AbstractPolicyTest {

	@Before
	public void setUp() throws Exception {
		super.setUp();
		policy = new HedgingZonePolicy();
	}
	
	@Test
	public void testNextItem() {
		
		Params params = mock(Params.class);
		when(params.getNumItems()).thenReturn(3);		
		//Item 2 has the largest backlog and the current setup (item 0) is at its target
		when(params.getSurplusTargets()).thenReturn(ImmutableList.of(0.0, 0.0, 0.0));
		when(params.getInitialDemand()).thenReturn(ImmutableList.of(0.0, 20.0, 30.0));
		when(params.getBacklogCosts()).thenReturn(ImmutableList.of(1.0, 2.0, 3.0));
		when(params.getInventoryHoldingCosts()).thenReturn(ImmutableList.of(1.0, 2.0, 3.0));
		when(params.getProductionRates()).thenReturn(ImmutableList.of(2.0, 4.0, 1.0));		
		PolicyParams policyParams = mock(PolicyParams.class);
		when(policyParams.getLowerHedgingPointsComputationMethod()).thenReturn(UserDefinedLowerHedgingPointsComputationMethod.class.getSimpleName());
		when(policyParams.getPriorityComparator()).thenReturn(CMuComparatorWithTiesById.class.getSimpleName());
		when(params.getPolicyParams()).thenReturn(policyParams);
	
		//Items 1 and 2 are outside their hedging zone. Item 2 has a larger backlog but item 1 has a larger cmu coeff
		when(policyParams.getUserDefinedLowerHedgingPoints()).thenReturn(Optional.of(ImmutableList.of(-5.0,-15.0,-10.0)));						
		fillRemainingMockedParams(params);
		Sim sim = getSim(params);
		policy.setUpPolicy(getSim(params));
		policy.currentSetup = sim.getMachine().getItemById(0);
		
		assertEquals("The next item should be item 1 because it has the largest cmu coefficient",
				1, policy.nextItem().getId());

		//Now put items 1 and 2 inside the hedging zone; we will choose based on the largest ratio of y/DZ
		when(params.getInitialDemand()).thenReturn(ImmutableList.of(0.0, 5.0, 5.0));
		policy.setUpPolicy(getSim(params));
		assertEquals("The next item should be item 2 because both are inside the hedging zone but 2 has larger y/DZ ratio",
				2, policy.nextItem().getId());
		
		//Now give items 1 and 2 the same y/DZ ratio. We should switch to 1 because that's the next one
		when(policyParams.getUserDefinedLowerHedgingPoints()).thenReturn(Optional.of(ImmutableList.of(-5.0,-15.0,-15.0)));
		policy.setUpPolicy(getSim(params));
		assertEquals("Items 1 and 2 are tied. Break ties by id", 1, policy.nextItem().getId());
	}

	@Test
	public void testIsTargetBased() {
		assertTrue("The HZP is target based", policy.isTargetBased());
	}

}


