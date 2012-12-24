/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * © 2012 Fernando Tubilla. All rights reserved.
 */

package sim;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class SimMain {

	public static Sim sim;
	
	public static void main(String[] args){
		
		//Configure the logger
		PropertyConfigurator.configure("config/log4j.properties");
		Logger logger = Logger.getLogger(SimMain.class);
		logger.info("Starting the simulation");
		
		sim = new Sim();
		SimSetup.setup(sim);
		SimRun.run(sim);
		
	}
	
	
}
