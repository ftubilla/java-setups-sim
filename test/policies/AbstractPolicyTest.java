package policies;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Test;

import params.Params;
import params.PolicyParams;
import sim.Clock;
import sim.Sim;
import system.Machine;

import com.google.common.collect.ImmutableList;

public abstract class AbstractPolicyTest extends TestCase {
	
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
		
		//Make sure that if it's not time to changeover, we return null
		assertNull("If it's not time to change over, nextItem() should return null", policy.nextItem());
	
		//Now move the targets down and check that we are still below
		when(params.getSurplusTargets()).thenReturn(ImmutableList.of(-10.0,-10.0));
		policy.setUpPolicy(getSim(params));
		assertTrue("The item is above its target", policy.isTimeToChangeOver());		
	}
	
	@Test
	public abstract void testNextItem();
	
	@Test
	public abstract void testIsTargetBased();
	
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
	
	/**
	 * Fills the remaining mocked params
	 * @param params
	 */
	protected void fillRemainingMockedParams(Params params) {
		int numItems = params.getNumItems();
		List<Double> onesDouble = new ArrayList<Double>();
		List<Double> zerosDouble = new ArrayList<Double>();
		List<Double> oneOverNumItemsDouble = new ArrayList<Double>();
		for (int i=0; i<numItems; i++){
			onesDouble.add(1.0);
			zerosDouble.add(0.0);
			oneOverNumItemsDouble.add(1.0 / numItems);
		}
		if (params.getBacklogCosts() == null || params.getBacklogCosts().isEmpty()) {
			when(params.getBacklogCosts()).thenReturn(ImmutableList.copyOf(onesDouble));
		}
		if (params.getInventoryHoldingCosts() == null || params.getInventoryHoldingCosts().isEmpty()) {
			when(params.getInventoryHoldingCosts()).thenReturn(ImmutableList.copyOf(onesDouble));
		}
		if (params.getInitialDemand() == null || params.getInitialDemand().isEmpty()) {
			when(params.getInitialDemand()).thenReturn(ImmutableList.copyOf(zerosDouble));
		}
		if (params.getProductionRates() == null || params.getProductionRates().isEmpty()) {
			when(params.getProductionRates()).thenReturn(ImmutableList.copyOf(onesDouble));
		}
		if (params.getDemandRates() == null || params.getDemandRates().isEmpty()) {
			when(params.getDemandRates()).thenReturn(ImmutableList.copyOf(oneOverNumItemsDouble));
		}
		if (params.getSetupTimes() == null || params.getSetupTimes().isEmpty()) {
			when(params.getSetupTimes()).thenReturn(ImmutableList.copyOf(onesDouble));
		}		
	}
	
	
}


