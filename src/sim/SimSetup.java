/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * © 2012 Fernando Tubilla. All rights reserved.
 */

package sim;

import java.io.File;
import system.Machine;
import scheduler.*;
import metrics.*;

import org.codehaus.jackson.map.ObjectMapper;

import output.Recorders;



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
		
		// TODO Send this to a different config file? that uses the MTTF and MTTR
		//Set the Failures/Repairs Generator
		//IRandomTimeIntervalGenerator failuresGenerator = new BinaryDistributedRandomTimeIntervalGenerator(1,4,0.5,6);
		long seed1 = System.currentTimeMillis();
		long seed2 = seed1 + 1;
		IRandomTimeIntervalGenerator failuresGenerator = new ExponentiallyDistributedRandomTimeIntervalGenerator(seed1,5);
		sim.setTheFailuresGenerator(failuresGenerator);
		
		//IRandomTimeIntervalGenerator repairsGenerator = new BinaryDistributedRandomTimeIntervalGenerator(1,1,1.0,0);
		IRandomTimeIntervalGenerator repairsGenerator = new ExponentiallyDistributedRandomTimeIntervalGenerator(seed2,1);
		sim.setTheRepairsGenerator(repairsGenerator);
		
		
		//Consistency checks
		
		// System has enough capacity
		double rho = 0;
		for (int i=0; i<sim.getParams().getNumItems(); i++){
			rho += sim.getParams().getDemandRates().get(i)/sim.getParams().getProductionRates().get(i);
		}
		
		
		double e = sim.getParams().getMeanTimeToFail()/(sim.getParams().getMeanTimeToFail() + sim.getParams().getMeanTimeToRepair());
		if (rho>=e){
			System.err.println("Warning: rho >= e!");
		}
		
		//Summarize system props
		System.out.println(sim);
		
		//Create the machine
		sim.setMachine(new Machine(sim.getParams()));

		//Load the policy
		sim.setTheScheduler(new RoundRobin());
		//sim.setTheScheduler(new UnstableExampleN3());
		
		
		
		//Setup the policy
		sim.getTheScheduler().setup(sim);
				
		
		//Initialize the metrics and recorders
		sim.setMetrics(new Metrics(sim));
		sim.setRecorders(new Recorders(sim));
		
		
	}
}
