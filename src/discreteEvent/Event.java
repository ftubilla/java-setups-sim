/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * © 2012 Fernando Tubilla. All rights reserved.
 */

package discreteEvent;

import org.apache.log4j.Logger;

import sim.Sim;

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

	protected double time;
	protected double deltaTime;
	private int id;
	private static int idCount = 0;

	public Event(double time) {
		this.time = time;
		this.id = idCount;
		Event.idCount++;
		logger.trace("Created " + this);
	}

	public int compareTo(Event otherEvent) {
		return (this.time < otherEvent.time ? -1 : (this.time == otherEvent.time ? 0 : 1));
	}

	public void handle(Sim sim) {
		beforeHandle(sim);
		if (logger.isDebugEnabled()) {
			logger.trace("Handling " + this);
		}
		deltaTime = time - sim.getTime();
		// Advance time
		logger.trace("Advancing sim time from " + sim.getTime() + " to " + time);
		sim.setTime(time);
		mainHandle(sim);
		sim.setLatestEvent(this);
		afterHandle(sim);
	}

	// This is the method that each new event should override
	protected abstract void mainHandle(Sim sim);

	private void beforeHandle(Sim sim) {
		for (IEventListener listener : sim.getListenersCoordinator().getBeforeEventListeners()) {
			logger.trace("Executing " + listener + " for " + this);
			listener.execute(this, sim);
		}
	}

	private void afterHandle(Sim sim) {
		for (IEventListener listener : sim.getListenersCoordinator().getAfterEventListener()) {
			logger.trace("Executing " + listener);
			listener.execute(this, sim);
		}
	}

	public double getTime() {
		return time;
	}
	
	protected void updateTime(double time) {
		if (logger.isTraceEnabled()) {
			logger.trace("Updating " + this + " time to " + time);
		}
		this.time = time;
	}

	public int getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ":" + id + " (" + time + ")";
	}

}
