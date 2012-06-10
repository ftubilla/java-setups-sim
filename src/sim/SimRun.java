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
		
		Event firstFailure = new Failure(sim.getTime() + sim.getTheFailuresGenerator().nextTimeInterval());
		sim.getFailuresSchedule().addEvent(firstFailure);
		
		while(sim.getTime() < 100 && !sim.eventsComplete()){
			sim.getNextEvent().handle(sim);
		}
		
		
		System.out.println("Total Events: " + Event.getCount());
		System.out.println("Total Failures: " + Failure.getCount());
		System.out.println("Total Repairs: " + Repair.getCount());
	}
	
}
