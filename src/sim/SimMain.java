/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * © 2012 Fernando Tubilla. All rights reserved.
 */

package sim;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import output.Recorders;
import util.JsonReader;

public class SimMain {

	public static Sim sim;
	
	public static void main(String[] args){
		
		//Configure the logger
		PropertyConfigurator.configure("config/log4j.properties");
		Logger logger = Logger.getLogger(SimMain.class);
		logger.info("Starting the simulation");
		
		//Get the params
		logger.info("Reading params from json file");
		Params params = JsonReader.readJson("inputs.json", Params.class);

		sim = new Sim(params);
		Recorders recorders = new Recorders();
		SimSetup.setup(sim, recorders);
		SimRun.run(sim, /*verbose*/ true);
		recorders.closeAll();
		
	}
	
	
}
