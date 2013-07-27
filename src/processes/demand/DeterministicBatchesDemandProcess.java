package processes.demand;

import java.util.HashMap;
import java.util.Map;

import sim.Sim;
import system.Item;
import discreteEvent.DemandArrival;

public class DeterministicBatchesDemandProcess implements IDemandProcess {

	private Map<Item, Integer> batchSizes;
	private Map<Item, Double> interArrivalTimes;

	@Override
	public DemandArrival getNextDemandArrival(Item item, double currentTime) {
		return new DemandArrival(item, currentTime
				+ interArrivalTimes.get(item), batchSizes.get(item));
	}

	@Override
	public void init(Sim sim) {

		batchSizes = new HashMap<Item, Integer>(sim.getMachine().getNumItems());
		interArrivalTimes = new HashMap<Item, Double>(sim.getMachine()
				.getNumItems());

		int batchSize = sim.getParams().getDemandProcessParams().getDemandBatchSize();
		for (Item item : sim.getMachine()) {
			batchSizes.put(item, batchSize);
			interArrivalTimes.put(item,
					batchSizes.get(item) / item.getDemandRate());
			sim.getMasterScheduler()
					.addEvent(
							new DemandArrival(item, sim.getTime()
									+ interArrivalTimes.get(item), batchSizes
									.get(item)));
		}
	}

	@Override
	public double minPossibleRate(Item item) {
		// Because the demand arrives in batches and there are finite periods
		// with no demand arriving, this rate can be 0.
		return 0;
	}

}
