/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * © 2012 Fernando Tubilla. All rights reserved.
 */

package sim;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.log4j.PropertyConfigurator;

import output.Recorders;
import params.Params;
import params.ParamsFactory;

/**
 * Main class for running a single or a series of simulation experiments in parallel.
 * @author ftubilla
 *
 */
@CommonsLog
public class SimMain {

	public static Sim sim;
	
	/**
	 * Execute this method for running the simulation. The arguments are
	 * a path to a directory with json's or a single json path, and an optional
	 * number of maximum parallel threads. The program will run a simulation
	 * per json file.
	 * 
	 * @param args
	 */
	public static void main(String[] args){
		
		//Configure the logger
		PropertyConfigurator.configure("config/log4j.properties");

		log.info("Starting the simulation experiment(s)");
		String inputsPath = args[0];
		log.info(String.format("Reading inputs from %s", inputsPath));
		int numThreads;
		try {
			numThreads = Integer.parseInt(args[1]);
		} catch (ArrayIndexOutOfBoundsException e) {
			numThreads = 1;
		}
		log.info(String.format("Using %d threads", numThreads));
		
		//Get the params
		ParamsFactory factory = new ParamsFactory(args[0]);
		Collection<Params> expParams = factory.make();
	
		final Recorders recorders = new Recorders();		
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		final ProgressBar bar = new ProgressBar(10, expParams.size());		
		
		System.out.println("****EXPERIMENT START****");
		bar.display();
		
		//Create the sim tasks
		for (final Params params : expParams){	
			Runnable worker = new Runnable(){
				@Override
				public void run(){
					Sim sim = new Sim(params);
					log.info(String.format("Created %s with %s", sim, params));
					SimSetup.setup(sim, recorders);
					sim.run(false);
					bar.addOneUnitOfProgress();
					bar.display();
				}
			};
			executor.execute(worker);			
		}

		executor.shutdown();
		while (!executor.isTerminated()){	
			/* Wait for experiment to finish */
		}		
		System.out.println("****EXPERIMENT COMPLETED!****");
		log.info("Finished experiment");
		recorders.closeAll();
				
	}
	
	
}
