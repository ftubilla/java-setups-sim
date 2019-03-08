package processes.production;

import java.util.HashMap;
import java.util.Map;

import discreteEvent.Event;
import discreteEvent.MasterScheduler;
import discreteEvent.ProductionDeparture;
import discreteEvent.ScheduleType;
import sim.Sim;
import sim.TimeInstant;
import system.Item;

public class DeterministicBatchesProductionProcess implements IProductionProcess {

	private Map<Item, Integer> batchSizes;
	private Map<Item, Double> interDepartureTimes;
	private MasterScheduler masterScheduler;

	@Override
	public ProductionDeparture getNextProductionDeparture(Item item, TimeInstant currentTime) {
		return new ProductionDeparture(item, currentTime.add(interDepartureTimes.get(item)), batchSizes.get(item));
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
	public boolean isDiscrete(){
		return true;
	}
	
	@Override
	public TimeInstant getNextScheduledProductionDepartureTime(Item item) {
		Event nextEvent = masterScheduler.getSchedule(ScheduleType.PRODUCTION).peekNextEvent();
		assert nextEvent instanceof ProductionDeparture;
		if (nextEvent==null || ((ProductionDeparture) nextEvent).getItem()!=item){
			//THere are no scheduled productions of this item
			return TimeInstant.INFINITY;
		}
		return nextEvent.getTime();
	}
	
	
}
