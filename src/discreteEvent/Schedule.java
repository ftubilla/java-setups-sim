/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * � 2012 Fernando Tubilla. All rights reserved.
 */

package discreteEvent;

import java.util.PriorityQueue;
import java.util.Queue;

import org.apache.log4j.Logger;

import sim.Clock;

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

	private boolean trace = logger.isTraceEnabled();
	private Queue<Event> eventQueue;
	private ScheduleType type;
	private boolean isOnHold;
	private Double onHoldSince;
	private Clock clock;
	private double lastEventTime=0;
	private double lastInterEventTime;


	public Schedule(ScheduleType type, Clock clock) {
		this.eventQueue = new PriorityQueue<Event>();
		this.type = type;
		logger.debug("Creating schedule " + type + " dumpable? "
				+ type.isDumpable() + " delayable? " + type.isDelayable());
		this.isOnHold = false;
		this.onHoldSince = null;
		this.clock = clock;
	}

	public void addEvent(Event e) {
		assert !isOnHold : "Cannot add events to a schedule that is on hold!";
		if (trace){logger.trace("Adding " + e + " to " + this);}
		this.eventQueue.add(e);
	}

	public Event getNextEvent() {
		if (!isOnHold){
			Event returnEvent = eventQueue.poll();
			if (trace){logger.trace("Returning " + returnEvent + " from " + this);}
			lastInterEventTime = returnEvent.time - lastEventTime;
			lastEventTime = returnEvent.time;			
			return returnEvent;
		} else {
			if (trace){logger.trace(this + " is on hold. Returning next event null");}
			return null;
		}
	}
	
	public Event peekNextEvent(){
		return eventQueue.peek();
	}

	public double nextEventTime() {
		double nextTime;
		if (eventQueue.isEmpty() || isOnHold){			
			nextTime = Double.MAX_VALUE;
		} else {
			nextTime = eventQueue.peek().getTime();
		}
		if (trace) {logger.trace("Next event time is " + nextTime + " for " + this);}
		return nextTime;
	}

	public boolean eventsComplete() {
		return this.eventQueue.isEmpty();
	}

	public void delayEvents(double delay) {
		assert type.isDelayable() : "Cannot delay " + this;
		assert !isOnHold : "Cannot delay a schedule that is currently on hold!";
		delayEventsRecursive(delay);
		logger.debug("Delayed all events in " + this + " by " + delay);
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
		logger.debug("Putting " + this + " on hold");
		isOnHold = true;
		onHoldSince = clock.getTime();
	}
	
	/**
	 * Use this method to release all locked events and delay them by the amount
	 * of time that they were on hold.
	 */
	public void releaseAndDelayEvents(){
		assert isOnHold : "Cannot release a schedule that is not on hold!";
		logger.debug("Releasing " + this + " and delaying its events");
		isOnHold = false;
		delayEvents(clock.getTime() - onHoldSince);
		onHoldSince = null;
	}
	
	public void dumpEvents() {
		assert type.isDumpable() : "Cannot dump this type of schedule!";
		logger.debug("Dumping all events in " + this);
		eventQueue.clear();
	}

	public ScheduleType getType() {
		return type;
	}
	
	public boolean isOnHold(){
		return isOnHold;
	}
	
	/**
	 * Returns the last interarrival time between two consecutive events in the same
	 * schedule.
	 * @return
	 */
	public double getLastInterEventTime(){
		return lastInterEventTime;
	}
	
	@Override
	public String toString(){
		return String.format("Schedule:%s (Events in queue: %d)", this.type, this.eventQueue.size());
	}
	
}
