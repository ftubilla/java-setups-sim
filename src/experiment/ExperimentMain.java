package experiment;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.codehaus.jackson.map.ObjectMapper;

import output.Recorders;
import sim.Params;
import sim.Sim;
import sim.SimRun;
import sim.SimSetup;


public class ExperimentMain {
	private static Logger logger;

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();

	@SuppressWarnings("unused")
	private boolean trace = logger.isTraceEnabled();
	
	
	public static void main(String[] args){
	
		PropertyConfigurator.configure("config/log4j.properties");	
		logger = Logger.getLogger(ExperimentMain.class);
		
		ExperimentParams expParams = null;

		// Read data
		try {
			ObjectMapper mapper = new ObjectMapper();
			expParams = mapper.readValue(new File("json/experiment.json"), ExperimentParams.class);
		} catch (Exception e) {
			logger.fatal("Problem reading input json files!");
			e.printStackTrace();
			System.exit(-1);
		}
		
		Recorders recorders = new Recorders();
		
		ExecutorService executor = Executors.newFixedThreadPool(1);
		for (int i=0; i<100; i++){
			
			Params simParams = null;
			try{
				ObjectMapper mapper = new ObjectMapper();
				simParams = mapper.readValue(new File("json/inputs.json"), Params.class);
				simParams.setSeedFailuresGenerator(i);
				simParams.setSeedFailuresGenerator(i+100);
				final Sim sim = new Sim(simParams);
				SimSetup.setup(sim, recorders);
				Runnable worker = new Runnable(){
					@Override
					public void run() {
						SimRun.run(sim);
					}};
				executor.execute(worker);
			} catch (Exception e){
				System.exit(-1);
			}
			
		}

		executor.shutdown();
		while (!executor.isTerminated()){
			
		}		
		logger.info("Finishing experiment");
		recorders.closeAll();
		
//		System.out.println("Combo Item Value");
//		int comboNum=0;
//		for (ParameterCombo paramCombo : params.getDemandRateCombos()){
//			for (int i=0; i<params.getNumItems(); i++){
//				String row = comboNum + " " + i + " " + paramCombo.get(i) + " ";
//				System.out.println(row);
//			}
//			comboNum++;
//		}
		
	}
	
	
}


