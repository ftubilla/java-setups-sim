/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * © 2012 Fernando Tubilla. All rights reserved.
 */

package sim;

import system.*;
import discreteEvent.*;

public class SimRun {

	public static void run(Sim sim){
		
		Item theItem = new Item(0, sim.getParams());
		System.out.println(theItem.getDemandRate());
		
		//Create a new event
//		Event failure = new Failure(10);
//		Event run = new Event(20);
//		
//		sim.getProductionSchedule().addEvent(failure);
//		sim.getProductionSchedule().addEvent(run);
//		
//		while(sim.getTime() < 100 && !sim.eventsComplete()){
//			
//			sim.getNextEvent().handle(sim);
//			
//		}
//		
//		System.out.println("Total Events: " + Event.getCount());
//		System.out.println("Total Failures: " + Failure.getCount());
//		System.out.println("Total Repairs: " + Repair.getCount());
		
	}
	
}
