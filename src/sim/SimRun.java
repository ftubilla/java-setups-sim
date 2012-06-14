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
		
		
		//TODO Send this to setup
		Event firstFailure = new Failure(sim.getTime() + sim.getTheFailuresGenerator().nextTimeInterval());
		sim.getFailuresSchedule().addEvent(firstFailure);
		sim.getProductionSchedule().addEvent(new ControlEvent(sim.getTime()));
		
		//TODO Figure out what to do with the metrics
		TimeMetricsRecorder timeMetricsRecorder = new TimeMetricsRecorder();
		
		while(sim.continueSim()){
			sim.getNextEvent().handle(sim);
			timeMetricsRecorder.record(sim);
		}
		
		timeMetricsRecorder.close();
		System.out.println("*****SIM COMPLETE!******************");
		System.out.println("Total Events: " + Event.getCount());
		System.out.println("Total Failures: " + Failure.getCount());
		System.out.println("Total Repairs: " + Repair.getCount());
		

	}
	
}
