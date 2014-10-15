package policies;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;

import params.Params;
import sim.Sim;

import com.google.common.collect.ImmutableList;

public class ClearTheLargestDeviationPolicyTest extends AbstractPolicyTest {

	@Before
	public void setUp() throws Exception {
		super.setUp();
		policy = new ClearTheLargestDeviationPolicy();
	}


	@Override
	public void testNextItem() {		
		Params params = mock(Params.class);		
		when(params.getNumItems()).thenReturn(3);
		
		//Item 2 has the largest backlog
		when(params.getSurplusTargets()).thenReturn(ImmutableList.of(0.0, 0.0, 0.0));
		when(params.getInitialDemand()).thenReturn(ImmutableList.of(10.0, 20.0, 21.0));
		fillRemainingMockedParams(params);
		Sim sim = getSim(params);
		policy.setUpPolicy(sim);
		policy.currentSetup = sim.getMachine().getItemById(0);
		assertEquals("Item 2 has the largest backlog and should be the next setup", policy.nextItem().getId(), 2);
		
		//All items have the same backlog, change to something different than the current setup and break ties by id
		when(params.getInitialDemand()).thenReturn(ImmutableList.of(0.0, 0.0, 0.0));
		policy.setUpPolicy(getSim(params));
		assertEquals("The next item should not be the current setup and ties should be broken by ID!", policy.nextItem().getId(), 1);
			
	}

	@Override
	public void testIsTargetBased() {
		assertTrue(policy.isTargetBased());
	}

}


