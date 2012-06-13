/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * © 2012 Fernando Tubilla. All rights reserved.
 */

package sim;

import java.io.File;
import system.Machine;
import system.scheduler.*;
import org.codehaus.jackson.map.ObjectMapper;


public class SimSetup {

	public static void setup(Sim sim){
		
		//Read data
		try{
			ObjectMapper mapper = new ObjectMapper();
			Params params = mapper.readValue(new File("json/inputs.json"), Params.class);
			sim.setParams(params);
		} 
		catch(Exception e){
			System.err.println("Problem reading input json file!");
		}
		
		
		//Set the Failures/Repairs Generator
		IRandomTimeIntervalGenerator failuresGenerator = new BinaryDistributedRandomTimeIntervalGenerator(1,4,0.5,6);
		sim.setTheFailuresGenerator(failuresGenerator);
		
		IRandomTimeIntervalGenerator repairsGenerator = new BinaryDistributedRandomTimeIntervalGenerator(1,1,1.0,0);
		sim.setTheRepairsGenerator(repairsGenerator);
		
		
		//Consistency checks
		
		// System has enough capacity
		double rho = 0;
		for (int i=0; i<sim.getParams().getNumItems(); i++){
			rho += sim.getParams().getDemandRates().get(i)/sim.getParams().getProductionRates().get(i);
		}
		
		
		double e = sim.getParams().getMeanTimeToFail()/(sim.getParams().getMeanTimeToFail() + sim.getParams().getMeanTimeToRepair());
		if (rho>=e){
			System.err.println("Check your data, rho >= e!");
			System.exit(-1);
		}
		
		//Summarize system props
		System.out.println(sim);
		
		//Create the machine
		sim.setMachine(new Machine(sim.getParams()));

		//Load the policy
		sim.setTheScheduler(new RoundRobin());
		
	}
}
