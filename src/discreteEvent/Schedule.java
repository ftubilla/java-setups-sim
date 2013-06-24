/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * � 2012 Fernando Tubilla. All rights reserved.
 */

package discreteEvent;

import java.util.*;

import org.apache.log4j.Logger;

import sim.Sim;

/**
 * A schedule keeps track of the upcoming events. If the schedule is dumpable,
 * then it is possible to remove all upcoming events, through the dumpEvents
 * method. If the schedule is delayable, then it is possible to either delay
 * events by a known amount in advance, or hold the events for an undefined
 * period and then release them with a delay equal to the amount of time that
 * they were held.
 * 
 * @author ftubilla
 * 
 */
public class Schedule {

	private static Logger logger = Logger.getLogger(Schedule.class);

	private Queue<Event> eventQueue;
	private ScheduleType type;
	private boolean isOnHold;
	private Double onHoldSince;


	public Schedule(ScheduleType type) {
		this.eventQueue = new PriorityQueue<Event>();
		this.type = type;
		logger.debug("Creating schedule " + type + " dumpable? "
				+ type.isDumpable() + " delayable? " + type.isDelayable());
		this.isOnHold = false;
		this.onHoldSince = null;
	}

	public void addEvent(Event e) {
		logger.debug("Adding event " + e.getClass().getSimpleName() + " "
				+ e.getId() + " to schedule " + type);
		this.eventQueue.add(e);
	}

	public Event getNextEvent() {
		if (!isOnHold){
			return this.eventQueue.poll();
		} else {
			return null;
		}
	}

	public double nextEventTime() {
		if (eventQueue.isEmpty() || isOnHold){
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
		assert !isOnHold : "Cannot delay a schedule that is currently on hold!";
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

	/**
	 * Use this method to lock all events in the schedule for some undefined
	 * period of time, but without dumping them. This method only works for
	 * delayable schedules.
	 */
	public void holdEvents(){
		assert type.isDelayable() : "Cannot hold a nondelayable schedule!";
		assert !isOnHold : "Schedule is already on hold!";
		logger.debug("Putting schedule " + type + " on hold");
		isOnHold = true;
		onHoldSince = Sim.time();
	}
	
	/**
	 * Use this method to release all locked events and delay them by the amount
	 * of time that they were on hold.
	 */
	public void releaseAndDelayEvents(){
		assert !isOnHold : "Cannot release a schedule that is not on hold!";
		logger.debug("Releasing schedule " + type + " and delaying its events");
		delayEvents(Sim.time() - onHoldSince);
		isOnHold = false;
		onHoldSince = null;
	}
	
	public void dumpEvents() {
		assert type.isDumpable() : "Cannot dump this type of schedule!";
		logger.debug("Dumping all events in schedule " + type);
		eventQueue.clear();
	}

	public ScheduleType getType() {
		return type;
	}
	
	public boolean isOnHold(){
		return isOnHold;
	}
}
