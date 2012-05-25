/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * © 2012 Fernando Tubilla. All rights reserved.
 */

package discreteEvent;

import sim.Sim;

public class Event implements Comparable<Event> {

	private int time;
	private int id;
	private static int idCount = 0;
	
	public Event(int time){
		this.time = time;
		this.id = idCount;
		Event.idCount++;
	}
	
	public int compareTo(Event otherEvent){
		return (this.time < otherEvent.time ? -1 : (this.time == otherEvent.time ? 0 : 1)); 				
	}
	
	public void handle(Sim sim){
		sim.setTime(time);
		System.out.println("Event " + this.id + " has occurred at time " + sim.getTime());
	}
		
	public int getTime() {
		return time;
	}

	public void updateTime(int time) {
		this.time = time;
	}

	public int getId() {
		return id;
	}
	
	public static int getCount(){
		return Event.idCount;
	}
	
}
