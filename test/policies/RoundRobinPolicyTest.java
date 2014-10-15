package policies;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import params.Params;
import params.PolicyParams;
import sim.Sim;

import com.google.common.collect.ImmutableList;

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
		Params params = mock(Params.class);
		when(params.getNumItems()).thenReturn(3);
		when(params.getInitialSetup()).thenReturn(0);
		when(params.getDemandRates()).thenReturn(ImmutableList.of(0.1,0.1,0.1));
		when(params.getProductionRates()).thenReturn(ImmutableList.of(1.0,1.0,1.0));
		when(params.getSetupTimes()).thenReturn(ImmutableList.of(1.0,1.0,1.0));
		when(params.getSurplusTargets()).thenReturn(ImmutableList.of(10.0,10.0,10.0));
		when(params.getInitialDemand()).thenReturn(ImmutableList.of(0.0,0.0,0.0));
		when(params.getInventoryHoldingCosts()).thenReturn(ImmutableList.of(1.0,1.0,1.0));
		when(params.getBacklogCosts()).thenReturn(ImmutableList.of(1.0,1.0,1.0));
		when(params.getPolicyParams()).thenReturn(mock(PolicyParams.class));
		Sim sim = getSim(params);
		policy.setUpPolicy(sim);
		policy.currentSetup = sim.getMachine().getItemById(0);
		assertTrue("The system is not ready for a new changeover, return null", policy.nextItem() == null);
		
		//Make the system ready for changeover
		when(params.getSurplusTargets()).thenReturn(ImmutableList.of(-10.0,-10.0,-10.0));
		sim = getSim(params);
		policy.setUpPolicy(sim);		
		assertEquals("The system is ready for a changeover, return the next item", policy.nextItem().getId(), 1);
		
		//Subsequent calls will return the same item
		assertEquals("The call to next Item is idempotent", policy.nextItem().getId(), 1);
		
		//Change the setup
		policy.currentSetup = sim.getMachine().getItemById(1);
		assertEquals("The system is ready for a changeover, return the next item", policy.nextItem().getId(), 2);
		
		policy.currentSetup = sim.getMachine().getItemById(2);
		assertEquals("The system is ready for a changeover, return the next item", policy.nextItem().getId(), 0);
		
	}
	
	
}


