/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * © 2012 Fernando Tubilla. All rights reserved.
 */

package discreteEvent;

import java.util.*;

public class Schedule {

	private Queue<Event> eventQueue;
	
	public Schedule(){
		this.eventQueue = new PriorityQueue<Event>();
	}
	
	public void addEvent(Event e){
		this.eventQueue.add(e);
	}
	
	public Event getNextEvent(){
		return this.eventQueue.poll();
	}
	
	public int nextEventTime(){
		return this.eventQueue.peek().getTime();
	}
	
	public boolean eventsComplete(){
		return this.eventQueue.isEmpty();
	}
	
	public void delayEvents(int delay){
		//Delays all events in the queue using recursion
		if(this.eventQueue.isEmpty()){
			return;
		}
		Event e = this.eventQueue.poll();
		delayEvents(delay);
		e.updateTime(e.getTime() + delay);
		this.eventQueue.add(e);
	}
		
}
