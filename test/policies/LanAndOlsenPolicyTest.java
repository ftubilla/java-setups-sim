package policies;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import lowerbounds.SurplusCostLowerBound;

import org.junit.Before;

import params.Params;
import sim.Sim;

import com.google.common.collect.ImmutableList;

public class LanAndOlsenPolicyTest extends AbstractPolicyTest {

	@Before
	public void setUp() throws Exception {
		super.setUp();
		policy = new LanAndOlsenPolicy();
	}


	@Override
	public void testNextItem() {		
		Params params = mock(Params.class);		
		when(params.getNumItems()).thenReturn(3);
		
		//Item 2 has the largest deviation ratio and the current setup (item 0) is at its target
		//Note that all items have the same value of Deviation + S*d
		when(params.getSurplusTargets()).thenReturn(ImmutableList.of(0.0, 0.0, 0.0));
		when(params.getInitialDemand()).thenReturn(ImmutableList.of(0.0, 20.0, 20.0));
		when(params.getSetupTimes()).thenReturn(ImmutableList.of(215.0, 15.0, 15.0));
		when(params.getDemandRates()).thenReturn(ImmutableList.of(0.1, 0.1, 0.1));
		SurplusCostLowerBound lowerBound = mock(SurplusCostLowerBound.class);
		when(lowerBound.getIdealSurplusDeviation(0)).thenReturn(1.0);
		when(lowerBound.getIdealSurplusDeviation(1)).thenReturn(2.0);
		when(lowerBound.getIdealSurplusDeviation(2)).thenReturn(0.5);
				
		fillRemainingMockedParams(params);
		Sim sim = getSim(params);
		when(sim.getSurplusCostLowerBound()).thenReturn(lowerBound);
		policy.setUpPolicy(sim);
		policy.currentSetup = sim.getMachine().getItemById(0);
		assertEquals("Item 2 has the largest deviation ratio and should be the next setup", 2, policy.nextItem().getId());
		
		//All items have the same deviation ratio, change to something different than the current setup and break ties by id
		when(params.getInitialDemand()).thenReturn(ImmutableList.of(0.0, 0.0, 0.0));
		when(lowerBound.getIdealSurplusDeviation(0)).thenReturn(1.0);
		when(lowerBound.getIdealSurplusDeviation(1)).thenReturn(1.0);
		when(lowerBound.getIdealSurplusDeviation(2)).thenReturn(1.0);
		sim = getSim(params);
		when(sim.getSurplusCostLowerBound()).thenReturn(lowerBound);
		policy.setUpPolicy(sim);
		assertEquals("The next item should not be the current setup and ties should be broken by ID!", 1, policy.nextItem().getId());
			
	}

	@Override
	public void testIsTargetBased() {
		assertTrue(policy.isTargetBased());
	}

}


