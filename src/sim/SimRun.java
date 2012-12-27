/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * © 2012 Fernando Tubilla. All rights reserved.
 */

package sim;

import org.apache.log4j.Logger;

import metrics.*;
import system.*;
import discreteEvent.*;
import output.*;

public class SimRun {

	private static Logger logger = Logger.getLogger(SimRun.class);
	
	public static void run(Sim sim){
		
		
		//TODO Send this to setup
		Event firstFailure = new Failure(sim.getTime() + sim.getTheFailuresGenerator().nextTimeInterval());
		sim.getMasterScheduler().addEvent(firstFailure);
		sim.getMasterScheduler().addEvent(new ControlEvent(sim.getTime()));

				
		// Main Loop of the Sim
		while(sim.continueSim()){
			
			logger.trace("Sim time: " + sim.getTime());
			if (sim.getTime()%1000 == 0){
				logger.info("Sim time: " + sim.getTime());
			}
			
			//Process the next event
			sim.getNextEvent().handle(sim);
			
			//Record metrics
			sim.getRecorders().getTimeMetricsRecorder().record(sim);
		}
		
		
		sim.getRecorders().closeAll();
		
		System.out.println("*****SIM COMPLETE!******************");
		System.out.println("Total Events: " + Event.getCount());
		System.out.println("Total Failures: " + Failure.getCount());
		System.out.println("Total Repairs: " + Repair.getCount());
		for (Item item : sim.getMachine()){
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
