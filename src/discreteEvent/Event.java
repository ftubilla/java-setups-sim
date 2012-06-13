/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * © 2012 Fernando Tubilla. All rights reserved.
 */

package discreteEvent;

import sim.Sim;
import system.*;

public class Event implements Comparable<Event> {

	private double time;
	private int id;
	private static int idCount = 0;


	
	public Event(double time){
		this.time = time;
		this.id = idCount;
		Event.idCount++;
	}
	
	public int compareTo(Event otherEvent){
		return (this.time < otherEvent.time ? -1 : (this.time == otherEvent.time ? 0 : 1)); 				
	}
	
	public void handle(Sim sim){
		
		//Update the items' surpluses
		for (Item item : sim.getMachine().getItemMap().values()){
			item.setCumulativeDemand(item.getCumulativeDemand() + item.getDemandRate()*(time-sim.getTime()));
			if (sim.getMachine().getSetup().equals(item)){
				switch(sim.getMachine().getOperationalState()){
					case SPRINT:
						item.setCumulativeProduction(item.getCumulativeProduction() + item.getProductionRate()*(time-sim.getTime()));
						break;
					case CRUISE:
						item.setCumulativeProduction(item.getCumulativeProduction() + item.getDemandRate()*(time-sim.getTime()));
						break;
				}
			}	
		}
		
		//Advance time
		System.out.println(this.getClass().getSimpleName() + " has occurred at time " + time);
		sim.setTime(time);
		sim.setLatestEvent(this);
	}
		
	public double getTime() {
		return time;
	}

	public void updateTime(double time) {
		this.time = time;
	}

	public int getId() {
		return id;
	}
	
	public static int getCount(){
		return Event.idCount;
	}
		
}
