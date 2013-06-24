package processes.demand;

import sim.Sim;
import system.Item;
import discreteEvent.DemandArrival;

/**
 * Interface for different types of demand processes.
 * 
 * @author ftubilla
 * 
 */
public interface IDemandProcess {

	/**
	 * Generates the next demand arrival event for the given item. This method
	 * is called after by the DemandArrival handle so that after each demand
	 * arrival we can add a new arrival to the master schedule.
	 * 
	 * @param item
	 * @param currentTime
	 *            of the sim
	 * @return DemandArrival event
	 */
	public DemandArrival getNextDemandArrival(Item item, double currentTime);

	/**
	 * Called at the beginning of the simulation to initialize the process.
	 * 
	 * @param sim
	 */
	public void init(Sim sim);

	/**
	 * Computes the minimum possible rate at which demand can accrue in the
	 * given process. For example, in a continuous deterministic model, this
	 * would correspond to the minimum slope or derivative of the cumulative
	 * demand process. In a discrete model, this rate is 0 because there are
	 * finite intervals during which no demand arrives.
	 * 
	 * @param item
	 * @param period
	 * @return
	 */
	public double minPossibleRate(Item item);

}
