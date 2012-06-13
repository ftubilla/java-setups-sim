/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * © 2012 Fernando Tubilla. All rights reserved.
 */

package sim;

import system.*;
import discreteEvent.*;
import output.*;

public class SimRun {

	public static void run(Sim sim){
		
		Event firstFailure = new Failure(sim.getTime() + sim.getTheFailuresGenerator().nextTimeInterval());
		sim.getFailuresSchedule().addEvent(firstFailure);
		
		FailureEventsRecorder test = new FailureEventsRecorder();
		EventsLengthRecorder test2 = new EventsLengthRecorder();
		
		while(sim.continueSim()){
			sim.getNextEvent().handle(sim);
			for (Item item : sim.getMachine().getItemMap().values()){
				System.out.println("Item " + item.getId() + " D: " + item.getCumulativeDemand() + " P:" + item.getCumulativeProduction());
			}
		}
		
		
		test.close();
		test2.close();
		
		
		System.out.println("Total Events: " + Event.getCount());
		System.out.println("Total Failures: " + Failure.getCount());
		System.out.println("Total Repairs: " + Repair.getCount());
		

	}
	
}
