/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * © 2012 Fernando Tubilla. All rights reserved.
 */

package sim;

import org.apache.log4j.Logger;

import discreteEvent.ControlEvent;
import discreteEvent.Event;
import discreteEvent.Failure;

public class SimRun {

	private static Logger logger = Logger.getLogger(SimRun.class);
		
	private static ProgressBar bar;
	
	public static void run(Sim sim){
		

		Event firstFailure = new Failure(sim.getTime() + sim.getTheFailuresGenerator().nextTimeInterval());
		sim.getMasterScheduler().addEvent(firstFailure);
		sim.getMasterScheduler().addEvent(new ControlEvent(sim.getTime()));
		bar = new ProgressBar(5, sim.getParams().getFinalTime());

		// Main Loop of the Sim
		while(sim.continueSim()){
			
			logger.trace("Sim time: " + sim.getTime());
			bar.display(sim.getTime());			
											
			//Process the next event
			try {
				sim.getNextEvent().handle(sim);
			} catch (NullPointerException e){
				logger.fatal("Event returned was null!");
				e.printStackTrace();
				System.exit(-1);
			}

		}
		
		bar.display(sim.getTime());
		
		sim.getRecorders().recordEndOfSim(sim);

		
		System.out.println("*****SIM COMPLETE!******************");
//		for (Item item : sim.getMachine()){
//			for (TimeFractionsMetrics.Metric metric : TimeFractionsMetrics.Metric.values()){
//				System.out.println(metric.toString() + " " + item.getId() + " " + 
//						sim.getMetrics().getTimeFractions().getMetricToItemToFraction().get(metric).get(item)/(sim.getTime()-Sim.METRICS_INITIAL_TIME));
//			}
//			System.out.println("Efficiency ei: " + 
//					sim.getMetrics().getTimeFractions().getMetricToItemToFraction().get(TimeFractionsMetrics.Metric.SPRINT).get(item)/
//					(sim.getMetrics().getTimeFractions().getMetricToItemToFraction().get(TimeFractionsMetrics.Metric.SPRINT).get(item) + 
//					sim.getMetrics().getTimeFractions().getMetricToItemToFraction().get(TimeFractionsMetrics.Metric.REPAIR).get(item)));
//			
//			System.out.println("Average Surplus deviation: " +
//					sim.getMetrics().getAverageSurplusMetrics().getAverageSurplusDeviation(item));
//			
//			System.out.println("Average Inventory: " + 
//					sim.getMetrics().getAverageSurplusMetrics().getAverageInventory(item));
//			
//			System.out.println("Average backlog: " + 
//					sim.getMetrics().getAverageSurplusMetrics().getAverageBacklog(item));
//			
//			System.out.println("---------------------------");
//		}

	}
	
}
