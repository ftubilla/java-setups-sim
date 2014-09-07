package experiment;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import output.Recorders;
import params.ExperimentSimParamsGenerator;
import params.Params;
import sim.ProgressBar;
import sim.Sim;
import sim.SimRun;
import sim.SimSetup;
import util.JsonReader;


public class ExperimentMain {
	
	private static Logger logger =  Logger.getLogger(ExperimentMain.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();

	@SuppressWarnings("unused")
	private boolean trace = logger.isTraceEnabled();
	
	
	public static void main(String[] args){
	
		PropertyConfigurator.configure("config/log4j.properties");	
		ExperimentParams expParams = JsonReader.readJson("experiment.json", ExperimentParams.class);		
		final Recorders recorders = new Recorders();		
		ExecutorService executor = Executors.newFixedThreadPool(expParams.getNumThreads());
		
		//Generate the parameter combinations
		List<Params> simParams = ExperimentSimParamsGenerator.generate(expParams);
		final ProgressBar bar = new ProgressBar(10, simParams.size());		
		
		System.out.println("****EXPERIMENT START****");
		bar.display();
		
		//Create the sim tasks
		for (final Params params : simParams){	
			Runnable worker = new Runnable(){
				@Override
				public void run(){
					Sim sim = new Sim(params);
					SimSetup.setup(sim, recorders);
					SimRun.run(sim, false);
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
		logger.info("Finished experiment");
		recorders.closeAll();
		

		
	}
	
	
}


