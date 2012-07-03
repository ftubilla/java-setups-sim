/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * © 2012 Fernando Tubilla. All rights reserved.
 */

package sim;

import metrics.*;
import system.*;
import discreteEvent.*;
import output.*;

public class SimRun {

	public static void run(Sim sim){
		
		
		//TODO Send this to setup
		Event firstFailure = new Failure(sim.getTime() + sim.getTheFailuresGenerator().nextTimeInterval());
		sim.getFailuresSchedule().addEvent(firstFailure);
		sim.getProductionSchedule().addEvent(new ControlEvent(sim.getTime()));
				
		while(sim.continueSim()){
			sim.getNextEvent().handle(sim);
			sim.getRecorders().getTimeMetricsRecorder().record(sim);
		}
		
		
		sim.getRecorders().closeAll();
		
		System.out.println("*****SIM COMPLETE!******************");
		System.out.println("Total Events: " + Event.getCount());
		System.out.println("Total Failures: " + Failure.getCount());
		System.out.println("Total Repairs: " + Repair.getCount());
		for (Item item : sim.getMachine().getItems()){
			for (TimeFractionsMetrics.Metric metric : TimeFractionsMetrics.Metric.values()){
				System.out.println(metric.toString() + " " + item.getId() + " " + 
						sim.getMetrics().getTimeFractions().getMetricToItemToFraction().get(metric).get(item)/(sim.getTime()-Sim.METRICS_INITIAL_TIME));
			}
			System.out.println("Efficiency ei: " + 
					sim.getMetrics().getTimeFractions().getMetricToItemToFraction().get(TimeFractionsMetrics.Metric.SPRINT).get(item)/
					(sim.getMetrics().getTimeFractions().getMetricToItemToFraction().get(TimeFractionsMetrics.Metric.SPRINT).get(item) + 
					sim.getMetrics().getTimeFractions().getMetricToItemToFraction().get(TimeFractionsMetrics.Metric.REPAIR).get(item)));
			
			System.out.println("---------------------------");
		}

	}
	
}
