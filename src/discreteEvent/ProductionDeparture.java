package discreteEvent;

import org.apache.log4j.Logger;

import sim.Sim;
import system.Item;

public class ProductionDeparture extends Event {

	private static Logger logger = Logger.getLogger(ProductionDeparture.class);
	private static int count;

	private Item item;
	private double productionAmount;

	public ProductionDeparture(Item item, double time, double productionAmount) {
		super(time);
		this.item = item;
		this.productionAmount = productionAmount;
		ProductionDeparture.count++;
		logger.debug("Creating production departure " + this.getId() + " for Item " + item.getId() + " with qty "
				+ productionAmount);
	}

	@Override
	public void mainHandle(Sim sim) {
		// Update cumulative production
		item.setCumulativeProduction(item.getCumulativeProduction() + productionAmount);
		// Generate next departure
		sim.getMasterScheduler().addEvent(sim.getProductionProcess().getNextProductionDeparture(item, sim.getTime()));
		// Generate a control event
		sim.getMasterScheduler().addEvent(new ControlEvent(sim.getTime()));
	}

	public Item getItem() {
		return item;
	}

	public double getProductionAmount() {
		return productionAmount;
	}

	public static int getCount() {
		return ProductionDeparture.count;
	}


}
