/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * © 2012 Fernando Tubilla. All rights reserved.
 */

package discreteEvent;

import java.util.*;

import org.apache.log4j.Logger;

/**
 * A schedule keeps track of the upcoming events. If the schedule is dumpable,
 * then it is possible to remove all upcoming events, through the dumpEvents
 * method.
 * 
 * @author ftubilla
 * 
 */
public class Schedule {

	private static Logger logger = Logger.getLogger(Schedule.class);

	private Queue<Event> eventQueue;
	private boolean dumpable = false;
	private String name;

	public Schedule(String name, boolean dumpable) {
		this.eventQueue = new PriorityQueue<Event>();
		this.name = name;
		this.dumpable = dumpable;
		logger.debug("Creating schedule " + name + " dumpable? " + dumpable);
	}

	public void addEvent(Event e) {
		logger.trace("Adding event " + e.getClass().getSimpleName() + " "
				+ e.getId() + " to schedule " + name);
		this.eventQueue.add(e);
	}

	public Event getNextEvent() {
		return this.eventQueue.poll();
	}

	public double nextEventTime() {
		return this.eventQueue.peek().getTime();
	}

	public boolean eventsComplete() {
		return this.eventQueue.isEmpty();
	}

	public void delayEvents(double delay) {
		// Delays all events in the queue using recursion
		if (this.eventQueue.isEmpty()) {
			logger.debug("Delayed all events in schedule " + name + " by "
					+ delay);
			return;
		}
		Event e = this.eventQueue.poll();
		delayEvents(delay);
		e.updateTime(e.getTime() + delay);
		this.eventQueue.add(e);
	}

	public void dumpEvents() {
		assert dumpable : "Cannot dump this type of schedule!";
		logger.debug("Dumping all events in schedule " + name);
		eventQueue.clear();
	}

	public String getName() {
		return this.getName();
	}
}
