package discreteEvent;

import org.apache.log4j.Logger;

import sim.Sim;
import system.Item;

public class DemandArrival extends Event {

	private static Logger logger = Logger.getLogger(DemandArrival.class);
	private static int count=0;
	
	private Item item;
	private double demand;

	/**
	 * An event corresponding to the arrival of a batch of demand for some item
	 * at a given time.
	 * 
	 * @param item
	 * @param time
	 * @param demand
	 */
	public DemandArrival(Item item, double time, double demand) {
		super(time);
		this.item = item;
		this.demand = demand;
		DemandArrival.count++;
		logger.debug("Creating demand arrival " + this.getId()
				+ " for Item " + item.getId() + " with qty " + demand);
	}

	@Override
	public void handle(Sim sim) {
		super.handle(sim);
		// Update cumulative demand
		item.setCumulativeDemand(item.getCumulativeDemand() + demand);
		// Generate next arrival
		sim.getMasterScheduler().addEvent(
				sim.getDemandProcess().getNextDemandArrival(item, sim.getTime()));
		// Generate a control event
		sim.getMasterScheduler().addEvent(new ControlEvent(sim.getTime()));
	}

	public Item getItem() {
		return item;
	}

	public double getDemand() {
		return demand;
	}

	public static int getCount() {
		return DemandArrival.count;
	}
}
