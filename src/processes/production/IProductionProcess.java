package processes.production;

import discreteEvent.ProductionDeparture;
import sim.Sim;
import sim.TimeInstant;
import system.Item;

/**
 * Interface for a production process that determines how much production is
 * generated per unit of work.
 * 
 * @author ftubilla
 *
 */
public interface IProductionProcess {

    public ProductionDeparture getNextProductionDeparture(Item item, TimeInstant currentTime);

    public void init(Sim sim);

    public boolean isDiscrete();

    public TimeInstant getNextScheduledProductionDepartureTime(Item item);

}
