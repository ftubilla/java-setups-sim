/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * � 2012 Fernando Tubilla. All rights reserved.
 */

package sim;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.PropertyConfigurator;

import com.google.common.io.Files;

import lombok.extern.apachecommons.CommonsLog;
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

	/**
	 * Execute this method for running the simulation. The arguments are
	 * a path to a directory with json's or a single json path, and an optional
	 * number of maximum parallel threads. The program will run a simulation
	 * per json file.
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
				
		//Get the application properties
		Properties properties = getProperties();
		
		//Configure the logger
		PropertyConfigurator.configure("config/log4j.properties");

		//Define the command line options
		//Options options = OptionBuilder.withArgName(name)("Number of threads").withArgName("threads")
		
		log.info("Starting the simulation experiment(s)");
		String inputsPath = String.format("%s%s%s", properties.getProperty("inputs.path"),
				File.separator, args[0]);
		log.info(String.format("Reading inputs from %s", inputsPath));
		int numThreads;
		try {
			numThreads = Integer.parseInt(args[1]);
		} catch (ArrayIndexOutOfBoundsException e) {
			numThreads = 1;
		}
		log.info(String.format("Using %d threads", numThreads));
		
		//Get the params
		ParamsFactory factory = new ParamsFactory(inputsPath);
		Collection<Params> expParams = factory.make();
	
		final Recorders recorders = new Recorders();		
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		final ProgressBar bar = new ProgressBar(10, expParams.size());		
		
		long startTime = System.currentTimeMillis();
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
		long endTime = System.currentTimeMillis();
		double elapsedSec = (endTime - startTime) / 1000.0; 
		System.out.println("****EXPERIMENT COMPLETED!****");
		System.out.println(String.format("Total time %.2f sec", elapsedSec));
		log.info(String.format("Finished experiment after %.2f sec", elapsedSec));
		recorders.closeAll();
		
		try {
			archiveOutput(properties.getProperty("archive.path"), args[0]);
		} catch (Exception e) {
			log.error("Could not archive output files!");
			e.printStackTrace();
		}
				
	}
	
	/**
	 * Copy all output files to the archive folder
	 */
	private static void archiveOutput(String archiveFolderPath, 
			String inputsFolderPath) throws Exception {
		
		//Get the name of the inputs folder
		String[] inputsPathComponents = inputsFolderPath.split(File.separator);
		String inputsFolderName = inputsPathComponents[inputsPathComponents.length - 1]; 
		
		//Make a directory in archive to hold the test results
		Date now = new Date();
		String timestamp = new SimpleDateFormat("yyyyMMddHHmm").format(now);
		File archiveDir = new File(String.format("%s%s%s_%s", archiveFolderPath, 
				File.separator, inputsFolderName, timestamp));
		archiveDir.mkdir();
		
		//Copy all files
		File outputDir = new File("output");		
		for (File outputFile : outputDir.listFiles()) {
			log.info(String.format("Copying %s to %s", outputFile.getPath(), archiveDir.getPath()));  
			File destFile = new File(archiveDir + File.separator + outputFile.getName());
			Files.copy(outputFile, destFile);
		}		
		
	}
	
	public static Properties getProperties() throws Exception {
		Properties properties = new Properties();
		String propFileName = "config/config.properties";
		try {
			Reader reader = new FileReader(propFileName);
			properties.load(reader);
		} catch (FileNotFoundException e) {
			throw new Exception("Could not find the properties file at config/config.properties!", e);	
		}
		return properties;
	}
	
}
