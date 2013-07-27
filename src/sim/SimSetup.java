/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * © 2012 Fernando Tubilla. All rights reserved.
 */

package sim;

import java.io.File;

import metrics.Metrics;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import output.Recorders;
import policies.IPolicy;
import policies.PolicyParams;
import processes.demand.DemandProcessParams;
import processes.demand.IDemandProcess;
import processes.generators.ExponentiallyDistributedRandomTimeIntervalGenerator;
import processes.generators.IRandomTimeIntervalGenerator;
import processes.production.IProductionProcess;
import processes.production.ProductionProcessParams;
import system.Machine;

public class SimSetup {

	private static Logger logger = Logger.getLogger(SimSetup.class);

	public static void setup(Sim sim) {

		// Read data
		try {
			ObjectMapper mapper = new ObjectMapper();
			Params params = mapper.readValue(new File("json/inputs.json"), Params.class);
			sim.setParams(params);
		} catch (Exception e) {
			logger.fatal("Problem reading input json file!");
			e.printStackTrace();
			System.exit(-1);
		}
		

		// Set the Failures/Repairs Generator
		long seedFailures = sim.getParams().getSeedFailuresGenerator();
		// IRandomTimeIntervalGenerator failuresGenerator = new
		// BinaryDistributedRandomTimeIntervalGenerator(seedFailures,4,0.5,6);
		IRandomTimeIntervalGenerator failuresGenerator = 
				new ExponentiallyDistributedRandomTimeIntervalGenerator(seedFailures,sim.getParams().getMeanTimeToFail());
		failuresGenerator.warmUp(100);
		sim.setTheFailuresGenerator(failuresGenerator);

		long seedRepairs = sim.getParams().getSeedRepairsGenerator();
		// IRandomTimeIntervalGenerator repairsGenerator = new
		// BinaryDistributedRandomTimeIntervalGenerator(seed,1,1.0,0);
		IRandomTimeIntervalGenerator repairsGenerator = 
				new ExponentiallyDistributedRandomTimeIntervalGenerator(seedRepairs,sim.getParams().getMeanTimeToRepair());
		repairsGenerator.warmUp(100);
		sim.setTheRepairsGenerator(repairsGenerator);

		// Consistency checks

		// System has enough capacity
		double rho = 0;
		for (int i = 0; i < sim.getParams().getNumItems(); i++) {
			rho += sim.getParams().getDemandRates().get(i) / sim.getParams().getProductionRates().get(i);
		}

		double e = sim.getParams().getMeanTimeToFail()
				/ (sim.getParams().getMeanTimeToFail() + sim.getParams().getMeanTimeToRepair());
		if (rho >= e) {
			System.err.println("Warning: rho >= e!");
		}

		// Summarize system props
		logger.info(sim);

		// Create the machine
		sim.setMachine(new Machine(sim.getParams(), sim.getMasterScheduler()));

		// Set up the demand process
		sim.setDemandProcess(ClassLoader.load("processes.demand", sim.getParams().getDemandProcessParams().getName(), 
				IDemandProcess.class));
		sim.getDemandProcess().init(sim);

		// Set up the production process
		sim.setProductionProcess(ClassLoader.load("processes.production", sim.getParams().getProductionProcessParams().getName(), 
				IProductionProcess.class));
		sim.getProductionProcess().init(sim);

		// Load the policy
		sim.setPolicy(ClassLoader.load("policies", sim.getParams().getPolicyParams().getName(), IPolicy.class));
		sim.getPolicy().setUp(sim);

		// Initialize the metrics and recorders
		sim.setMetrics(new Metrics(sim));
		sim.setRecorders(new Recorders(sim));

	}
}
