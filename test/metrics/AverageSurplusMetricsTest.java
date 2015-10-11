package metrics;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import output.Recorders;
import params.Params;
import params.ParamsFactory;
import sim.Sim;
import sim.SimSetup;
import system.Item;
import util.SimBasicTest;

public class AverageSurplusMetricsTest extends SimBasicTest {

	private Params params;
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		ParamsFactory factory = new ParamsFactory("test/resources/base_3_items.json");
		Collection<Params> paramsCollection = factory.make();
		params = spy(paramsCollection.iterator().next());
	}
	
	@Test
	public void testOneTimeStep() {

		//First set the surplus targets to 0
		when(params.getSurplusTargets()).thenReturn(ImmutableList.of(0.0, 0.0, 0.0));
		when(params.getInitialDemand()).thenReturn(ImmutableList.of(100.0, 100.0, 100.0));
		when(params.getDemandRates()).thenReturn(ImmutableList.of(0.0, 0.0, 0.0));
		when(params.getProductionRates()).thenReturn(ImmutableList.of(1.0, 1.0, 1.0));
		when(params.getInitialSetup()).thenReturn(0);
		when(params.getFinalTime()).thenReturn(100.0);
		
		Sim sim = new Sim(params);
		SimSetup.setup(sim, new Recorders(Collections.emptyList()));
		sim.run(false);
		AverageSurplusMetrics metrics = sim.getMetrics().getAverageSurplusMetrics();
		Item item0 = sim.getMachine().getItemById(0);
		assertWithinTolerance( metrics.getAverageBacklog(item0), 50.0, 1e-3 );
		assertWithinTolerance( metrics.getAverageInventory(item0), 0.0, 1e-3 );
		assertWithinTolerance( metrics.getAverageServiceLevel(item0), 0.0, 1e-3);
		
		//Now run longer and with inventory during the second half
		when(params.getSurplusTargets()).thenReturn(ImmutableList.of(100.0, 100.0, 100.0));
		when(params.getFinalTime()).thenReturn(200.0);
		sim = new Sim(params);
		SimSetup.setup(sim, new Recorders(Collections.emptyList()));
		sim.run(false);
		metrics = sim.getMetrics().getAverageSurplusMetrics();
		item0 = sim.getMachine().getItemById(0);
		assertWithinTolerance( metrics.getAverageBacklog(item0), 25.0, 1e-3 );
		assertWithinTolerance( metrics.getAverageInventory(item0), 25.0, 1e-3 );
		assertWithinTolerance( metrics.getAverageServiceLevel(item0), 0.5, 1e-3);
		
	}
	
}
