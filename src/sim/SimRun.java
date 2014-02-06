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
	
	public static void run(Sim sim, boolean verbose){
		
		Event firstFailure = new Failure(sim.getTime() + sim.getTheFailuresGenerator().nextTimeInterval());
		sim.getMasterScheduler().addEvent(firstFailure);
		sim.getMasterScheduler().addEvent(new ControlEvent(sim.getTime()));
		bar = new ProgressBar(5, sim.getParams().getFinalTime());

		// Main Loop of the Sim
		while(sim.continueSim()){
			
			logger.trace("Sim time: " + sim.getTime());
			if (verbose) {
				bar.setProgress(sim.getTime());
				bar.display();
			}			
											
			//Process the next event
			try {
				sim.getNextEvent().handle(sim);
			} catch (NullPointerException e){
				logger.fatal("Event returned was null!");
				e.printStackTrace();
				System.exit(-1);
			}

		}
		
		if (verbose) {
			bar.setProgress(sim.getTime());
			bar.display();
		}
				
		sim.getRecorders().recordEndOfSim(sim);

	}
	
}
