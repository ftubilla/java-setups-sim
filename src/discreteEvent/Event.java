/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * © 2012 Fernando Tubilla. All rights reserved.
 */

package discreteEvent;

import java.util.ArrayList;
import java.util.List;

import metrics.TimeFractionsMetrics;

import org.apache.log4j.Logger;

import sim.Sim;
import system.Item;
import system.Machine.FailureState;

/**
 * The base class from which any other type of event should inherit. Takes care
 * of calling listeners (if any) at the beginning of the processing of the
 * event.
 * 
 * @author ftubilla
 * 
 */
public abstract class Event implements Comparable<Event> {

	private static Logger logger = Logger.getLogger(Event.class);

	private static List<BeforeEventListener> beforeEventListeners;

	static {
		beforeEventListeners = new ArrayList<BeforeEventListener>();
	}

	protected double time;
	protected double deltaTime;
	private int id;
	private static int idCount = 0;

	public Event(double time) {
		this.time = time;
		this.id = idCount;
		Event.idCount++;
		logger.debug("Created new event " + this.getClass() + " " + id
				+ " for time " + time);
	}

	public int compareTo(Event otherEvent) {
		return (this.time < otherEvent.time ? -1
				: (this.time == otherEvent.time ? 0 : 1));
	}
	
	public void handle(Sim sim){
		beforeHandle(sim);
		if (logger.isDebugEnabled()){logger.debug("Handling " + this);}		
		mainHandle(sim);
		afterHandle(sim);
	}
	
	//This is the method that each new event should override
	protected abstract void mainHandle(Sim sim); 
	
	private void beforeHandle(Sim sim) {
		deltaTime = time - sim.getTime();
		for (BeforeEventListener listener : beforeEventListeners) {
			logger.debug("Executing before event listener " + listener.getId());
			listener.execute(this);
		}
		
		// Check if it's time to start recording data
				if (!Sim.TIME_TO_START_RECORDING
						&& sim.getTime() >= sim.getParams().getMetricsStartTime()) {
					logger.debug("Time to start recording data. Sim time: "
							+ sim.getTime());
					Sim.METRICS_INITIAL_TIME = sim.getTime();
					Sim.TIME_TO_START_RECORDING = true;
				}

				// Update the items' surpluses
				for (Item item : sim.getMachine()) {

					// Execute if the machine has this setup
					if (sim.getMachine().getSetup().equals(item)) {

						// Machine up
						if (sim.getMachine().getFailureState() == FailureState.UP) {

							switch (sim.getMachine().getOperationalState()) {
							case SPRINT:
							//TODO Remove these commented lines and move the metrics to a listener.
//								item.setCumulativeProduction(item
//										.getCumulativeProduction()
//										+ item.getProductionRate() * deltaTime);
								// Update Metrics
								sim.getMetrics()
										.getTimeFractions()
										.increment(TimeFractionsMetrics.Metric.SPRINT,
												item, deltaTime);
								break;

							case CRUISE:
//								item.setCumulativeProduction(item
//										.getCumulativeProduction()
//										+ item.getDemandRate() * deltaTime);
								// Update Metrics
								sim.getMetrics()
										.getTimeFractions()
										.increment(TimeFractionsMetrics.Metric.CRUISE,
												item, deltaTime);
								break;

							case IDLE:
								// Update Metrics
								sim.getMetrics()
										.getTimeFractions()
										.increment(TimeFractionsMetrics.Metric.IDLE,
												item, deltaTime);
								break;

							case SETUP:
								// Update Metrics
								sim.getMetrics()
										.getTimeFractions()
										.increment(TimeFractionsMetrics.Metric.SETUP,
												item, deltaTime);
							}

						} else {
							// Machine down
							sim.getMetrics()
									.getTimeFractions()
									.increment(TimeFractionsMetrics.Metric.REPAIR,
											item, deltaTime);
						}
					}
				}

				// Call other recorders
				sim.getRecorders().getFailureEventsRecorder().record(sim);
				sim.getRecorders().getEventsLengthRecorder()
						.record(this.getClass().getSimpleName(), deltaTime);
	}
	
	private void afterHandle(Sim sim){
		// Advance time
		logger.debug("Advancing sim time from " + sim.getTime() + " to " + time);
		sim.setTime(time);
		sim.setLatestEvent(this);
	}
	


	public double getTime() {
		return time;
	}

	public void updateTime(double time) {
		if (logger.isTraceEnabled()){logger.trace("Updating " + this + " time to " + time);}
		this.time = time;
	}

	public int getId() {
		return id;
	}
	
	public String toString(){
		return this.getClass().getSimpleName() + ":" + id;
	}

	public static int getCount() {
		return Event.idCount;
	}

	/**
	 * Adds a listener that is executed before handling any event.
	 * 
	 * @param listener
	 */
	public static void addBeforeEventListener(BeforeEventListener listener) {
		logger.debug("Adding BeforeEventListener " + listener.getId());
		beforeEventListeners.add(listener);
	}

}
