package processes.production;

import java.util.HashMap;
import java.util.Map;

import sim.Sim;
import system.Item;
import discreteEvent.MasterScheduler;
import discreteEvent.ProductionDeparture;

public class DeterministicBatchesProductionProcess implements IProductionProcess {

	private Map<Item, Integer> batchSizes;
	private Map<Item, Double> interDepartureTimes;
	private MasterScheduler masterScheduler;

	@Override
	public ProductionDeparture getNextProductionDeparture(Item item, double currentTime) {
		return new ProductionDeparture(item, currentTime + interDepartureTimes.get(item), batchSizes.get(item));
	}

	@Override
	public void init(Sim sim) {

		batchSizes = new HashMap<Item, Integer>(sim.getMachine().getNumItems());
		interDepartureTimes = new HashMap<Item, Double>(sim.getMachine().getNumItems());
		masterScheduler = sim.getMasterScheduler();
		for (Item item : sim.getMachine()) {
			batchSizes.put(item,sim.getParams().getProductionProcessParams().getProductionBatchSize());
			interDepartureTimes.put(item, batchSizes.get(item) / item.getProductionRate());
		}
		//Add the first production
		masterScheduler.addEvent(getNextProductionDeparture(sim.getMachine().getSetup(), sim.getTime()));
	}

	@Override
	public double maxPossibleRate(Item item) {
		// Because a finite amount of production departs in an infinitesimal
		// period, in theory the max rate here is infinity.
		return Double.MAX_VALUE;
	}

}
