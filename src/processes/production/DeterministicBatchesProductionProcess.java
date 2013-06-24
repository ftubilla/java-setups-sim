package processes.production;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import sim.Sim;
import system.Item;
import system.Machine;
import system.Machine.OperationalState;
import discreteEvent.MasterScheduler;
import discreteEvent.ProductionDeparture;

public class DeterministicBatchesProductionProcess implements IProductionProcess {

	private Map<Item, Double> batchSizes;
	private Map<Item, Double> interDepartureTimes;
	private MasterScheduler masterScheduler;

	@Override
	public ProductionDeparture getNextProductionDeparture(Item item, double currentTime) {
		return new ProductionDeparture(item, currentTime + interDepartureTimes.get(item), batchSizes.get(item));
	}

	@Override
	public void init(Sim sim) {

		batchSizes = new HashMap<Item, Double>(sim.getMachine().getNumItems());
		interDepartureTimes = new HashMap<Item, Double>(sim.getMachine().getNumItems());
		masterScheduler = sim.getMasterScheduler();
		Random generator = new Random();
		for (Item item : sim.getMachine()) {
			batchSizes.put(item, 4.0/* generator.nextDouble() */);
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
