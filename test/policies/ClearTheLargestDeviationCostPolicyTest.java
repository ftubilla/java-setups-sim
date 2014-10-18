package policies;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;

import params.Params;
import sim.Sim;

import com.google.common.collect.ImmutableList;

public class ClearTheLargestDeviationCostPolicyTest extends AbstractPolicyTest {

	@Before
	public void setUp() throws Exception {
		super.setUp();
		policy = new ClearTheLargestDeviationPolicy();
	}


	@Override
	public void testNextItem() {		
		Params params = mock(Params.class);		
		when(params.getNumItems()).thenReturn(3);
		
		//Item 2 has the largest backlog cost and item 0 (the current setup) is at its target
		when(params.getSurplusTargets()).thenReturn(ImmutableList.of(0.0, 0.0, 0.0));
		when(params.getInitialDemand()).thenReturn(ImmutableList.of(0.0, 10.0, 10.0));
		when(params.getBacklogCosts()).thenReturn(ImmutableList.of(1.0, 3.5, 3.0));
		//This policy should ignore inventory costs
		when(params.getInventoryHoldingCosts()).thenReturn(ImmutableList.of(1.0, 0.0, 1.0));
		fillRemainingMockedParams(params);
		Sim sim = getSim(params);
		policy.setUpPolicy(sim);
		policy.currentSetup = sim.getMachine().getItemById(0);
		assertEquals("Item 1 has the largest backlog cost and should be the next setup", 1, policy.nextItem().getId());
		
		//All items have the same backlog, change to something different than the current setup and break ties by id
		when(params.getBacklogCosts()).thenReturn(ImmutableList.of(0.0, 0.0, 0.0));
		policy.setUpPolicy(getSim(params));
		assertEquals("The next item should not be the current setup and ties should be broken by ID!", 1, policy.nextItem().getId());
			
	}

	@Override
	public void testIsTargetBased() {
		assertTrue(policy.isTargetBased());
	}

}


