package processes.demand;

import java.util.HashMap;
import java.util.Map;

import discreteEvent.DemandArrival;
import sim.Sim;
import sim.TimeInstant;
import system.Item;

public class DeterministicBatchesDemandProcess implements IDemandProcess {

    private Map<Item, Integer>       batchSizes;
    private Map<Item, Double>        interArrivalTimes;
    private Map<Item, DemandArrival> scheduledArrivals;

    @Override
    public DemandArrival getNextDemandArrival(Item item, TimeInstant currentTime) {
        DemandArrival arrival = new DemandArrival(item, currentTime.add(interArrivalTimes.get(item)),
                batchSizes.get(item));
        scheduledArrivals.put(item, arrival);
        return arrival;
    }

    @Override
    public void init(Sim sim) {

        batchSizes = new HashMap<Item, Integer>(sim.getMachine().getNumItems());
        interArrivalTimes = new HashMap<Item, Double>(sim.getMachine().getNumItems());
        scheduledArrivals = new HashMap<Item, DemandArrival>();

        int batchSize = sim.getParams().getDemandProcessParams().getDemandBatchSize();
        for (Item item : sim.getMachine()) {
            batchSizes.put(item, batchSize);
            interArrivalTimes.put(item, batchSizes.get(item) / item.getDemandRate());
            DemandArrival arrival = new DemandArrival(item, sim.getTime().add(interArrivalTimes.get(item)),
                    batchSizes.get(item));
            sim.getMasterScheduler().addEvent(arrival);
            scheduledArrivals.put(item, arrival);
        }
    }

    @Override
    public boolean isDiscrete() {
        return true;
    }

    @Override
    public TimeInstant getNextScheduledDemandArrivalTime(Item item) {
        return scheduledArrivals.get(item).getTime();
    }

}
