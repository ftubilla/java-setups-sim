/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * © 2012 Fernando Tubilla. All rights reserved.
 */

package discreteEvent;

import java.util.*;

public class Schedule {

	private Queue<Event> eventQueue;
	private boolean dumpable = false;
	
	public Schedule(){
		this.eventQueue = new PriorityQueue<Event>();
	}
	
	public Schedule(boolean dumpable){
		this();
		this.dumpable = dumpable;
	}
	
	
	public void addEvent(Event e){
		this.eventQueue.add(e);
	}
	
	public Event getNextEvent(){
		return this.eventQueue.poll();
	}
	
	public double nextEventTime(){
		return this.eventQueue.peek().getTime();
	}
	
	public boolean eventsComplete(){
		return this.eventQueue.isEmpty();
	}
	
	public void delayEvents(double delay){
		//Delays all events in the queue using recursion
		if(this.eventQueue.isEmpty()){
			return;
		}
		Event e = this.eventQueue.poll();
		delayEvents(delay);
		e.updateTime(e.getTime() + delay);
		this.eventQueue.add(e);
	}
	
	public void dumpEvents(){
		if (dumpable){
			this.eventQueue.clear();
		} else{
			System.err.println("Cannot dump this type of schedule!");
			System.exit(-1);
		}
	}	
}
