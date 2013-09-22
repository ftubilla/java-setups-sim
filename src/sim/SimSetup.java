/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * © 2012 Fernando Tubilla. All rights reserved.
 */

package sim;

import java.util.Random;

import metrics.Metrics;

import org.apache.log4j.Logger;

import output.Recorders;
import policies.IPolicy;
import processes.demand.IDemandProcess;
import processes.generators.ExponentiallyDistributedRandomTimeIntervalGenerator;
import processes.generators.IRandomTimeIntervalGenerator;
import processes.production.IProductionProcess;
import system.Item;
import system.Machine;
import discreteEvent.AfterEventListener;
import discreteEvent.BeforeEventListener;
import discreteEvent.Event;

public class SimSetup {

	private static Logger logger = Logger.getLogger(SimSetup.class);
		
	public static void setup(Sim sim, Recorders recorders) {

		// Set the Failures/Repairs generators
		
		Random seedGenerator = new Random(sim.getParams().getSeed());
		
		long seedFailures = seedGenerator.nextLong();
		IRandomTimeIntervalGenerator failuresGenerator = 
				new ExponentiallyDistributedRandomTimeIntervalGenerator(seedFailures,sim.getParams().getMeanTimeToFail());
		failuresGenerator.warmUp(100);
		sim.setTheFailuresGenerator(failuresGenerator);

		long seedRepairs = seedGenerator.nextLong();
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
		sim.setMachine(new Machine(sim.getParams(), sim.getClock(), sim.getMasterScheduler()));
		
		// Set up the demand process
		sim.setDemandProcess(AlgorithmLoader.load("processes.demand", sim.getParams().getDemandProcessParams().getName(), 
				IDemandProcess.class));
		sim.getDemandProcess().init(sim);
		
		// Set up the production process
		sim.setProductionProcess(AlgorithmLoader.load("processes.production", sim.getParams().getProductionProcessParams().getName(), 
				IProductionProcess.class));
		sim.getProductionProcess().init(sim);
			
		assert (sim.getProductionProcess().isDiscrete() && sim.getDemandProcess().isDiscrete()) || 
			(!sim.getProductionProcess().isDiscrete() && !sim.getDemandProcess().isDiscrete()) :
				"Mixed discrete and continuous processes is not supported!";
			
		// Load the policy
		sim.setPolicy(AlgorithmLoader.load("policies", sim.getParams().getPolicyParams().getName(), IPolicy.class));
		sim.getPolicy().setUpPolicy(sim);
		if (sim.getPolicy().isTargetBased()){
			for (Item item : sim.getMachine()){
				assert item.getSurplusDeviation() >= 0 : "Cannot start above the target for a target-based policy!";
			}
		}
		
		// Initialize the metrics and recorders
		sim.setMetrics(new Metrics(sim));
		sim.setRecorders(recorders);
		
		//The lines below make sure that the recorders' methods are called before/after each event
		Event.addBeforeEventListener(new BeforeEventListener(){
			@Override
			public void execute(Event event, Sim sim) {
				sim.getRecorders().recordBeforeEvent(sim, event);
			}			
		}, sim);
		Event.addAfterEventListener(new AfterEventListener(){
			@Override
			public void execute(Event event, Sim sim) {
				sim.getRecorders().recordAfterEvent(sim, event);
			}			
		}, sim);
		

	}
}
