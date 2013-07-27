package processes.production;

import sim.Sim;
import system.Item;
import discreteEvent.ProductionDeparture;

/**
 * Interface for a production process that determines how much production is generated per unit of work.
 * @author ftubilla
 *
 */
public interface IProductionProcess {
	
	public ProductionDeparture getNextProductionDeparture(Item item, double currentTime);
	
	public void init(Sim sim);
	
	public double maxPossibleRate(Item item);

}
