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
	private ScheduleType type;

	public Schedule(ScheduleType type) {
		this.eventQueue = new PriorityQueue<Event>();
		this.type = type;
		logger.debug("Creating schedule " + type + " dumpable? "
				+ type.isDumpable() + " delayable? " + type.isDelayable());
	}

	public void addEvent(Event e) {
		logger.debug("Adding event " + e.getClass().getSimpleName() + " "
				+ e.getId() + " to schedule " + type);
		this.eventQueue.add(e);
	}

	public Event getNextEvent() {
		return this.eventQueue.poll();
	}

	public double nextEventTime() {
		if (eventQueue.isEmpty()){
			logger.trace("Next event time is infinity for Schedule " + this.getType());
			return Double.MAX_VALUE;
		} else {
		return this.eventQueue.peek().getTime();
		}
	}

	public boolean eventsComplete() {
		return this.eventQueue.isEmpty();
	}

	public void delayEvents(double delay) {
		assert type.isDelayable() : "Cannot delay this type of schedule!";
		delayEventsRecursive(delay);
		logger.debug("Delayed all events in schedule " + type + " by "
				+ delay);
	}
	
	private void delayEventsRecursive(double delay){
		// Delays all events in the queue using recursion
		if (this.eventQueue.isEmpty()) {
			return;
		}
		Event e = this.eventQueue.poll();
		delayEventsRecursive(delay);
		e.updateTime(e.getTime() + delay);
		this.eventQueue.add(e);
	}

	public void dumpEvents() {
		assert type.isDumpable() : "Cannot dump this type of schedule!";
		logger.debug("Dumping all events in schedule " + type);
		eventQueue.clear();
	}

	public ScheduleType getType() {
		return type;
	}
}
