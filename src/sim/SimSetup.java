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
import policies.RoundRobinPolicy;
import processes.demand.ContinuousDemandProcess;
import processes.generators.ExponentiallyDistributedRandomTimeIntervalGenerator;
import processes.generators.IRandomTimeIntervalGenerator;
import processes.production.ContinuousProductionProcess;
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
		// sim.setDemandProcess(new DeterministicBatchesDemandProcess());
		sim.setDemandProcess(new ContinuousDemandProcess());

		sim.getDemandProcess().init(sim);

		// Set up the production process
		sim.setProductionProcess(new ContinuousProductionProcess());
		// sim.setProductionProcess(new DeterministicBatchesProductionProcess());

		sim.getProductionProcess().init(sim);

		// Load the policy
		sim.setPolicy(new RoundRobinPolicy());

		// Set up the policy
		sim.getPolicy().setUp(sim);

		// Initialize the metrics and recorders
		sim.setMetrics(new Metrics(sim));
		sim.setRecorders(new Recorders(sim));

	}
}
