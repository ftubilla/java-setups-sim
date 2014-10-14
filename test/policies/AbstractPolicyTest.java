package policies;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import params.Params;
import params.PolicyParams;
import sim.Clock;
import sim.Sim;
import system.Machine;

public class AbstractPolicyTest extends TestCase {
	
	protected AbstractPolicy policy;
	
	@Before
	public void setUp() throws Exception {	
		PropertyConfigurator.configure("config/log4j.properties");		
	}
	
	@Test
	public void testPolicyNotNull() {
		assertFalse("Initialize the policy in setUp()", policy == null);
	}

	@Test
	public void testIsTimeToChangeOver() {		
		//Create a two-item system with the first item below its target
		Params params = mock(Params.class);
		when(params.getNumItems()).thenReturn(2);
		when(params.getInitialSetup()).thenReturn(0);
		when(params.getDemandRates()).thenReturn(ImmutableList.of(0.1,0.1));
		when(params.getProductionRates()).thenReturn(ImmutableList.of(1.0,1.0));
		when(params.getSetupTimes()).thenReturn(ImmutableList.of(1.0,1.0));
		when(params.getSurplusTargets()).thenReturn(ImmutableList.of(10.0,10.0));
		when(params.getInitialDemand()).thenReturn(ImmutableList.of(0.0,0.0));
		when(params.getInventoryHoldingCosts()).thenReturn(ImmutableList.of(1.0,1.0));
		when(params.getBacklogCosts()).thenReturn(ImmutableList.of(1.0,1.0));
		when(params.getPolicyParams()).thenReturn(mock(PolicyParams.class));
		policy.setUpPolicy(getSim(params));
		assertFalse("The item is still below target", policy.isTimeToChangeOver());
	
		//Now move the targets down and check that we are still below
		when(params.getSurplusTargets()).thenReturn(ImmutableList.of(-10.0,-10.0));
		policy.setUpPolicy(getSim(params));
		assertTrue("The item is above its target", policy.isTimeToChangeOver());		
	}
	
	/**
	 * Creates a mocked Sim instance based on the given params.
	 * 
	 * @param params
	 * @return Sim
	 */
	protected Sim getSim(Params params) {
		Machine machine = new Machine(params, new Clock(0.0), null);
		Sim sim = mock(Sim.class);
		when(sim.getMachine()).thenReturn(machine);
		when(sim.hasDiscreteMaterial()).thenReturn(false);
		when(sim.getParams()).thenReturn(params);		
		return sim;
	}
	
	
	
}


