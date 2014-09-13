package sim;

import output.Recorders;
import params.Params;
import system.Item;
import util.JsonReader;

/**
 * Provides a wrapper to run the sim from within R
 * 
 * @author ftubilla
 *
 */
public class RSimRun {
		
	public double[] run(String json){
		
		Params params = JsonReader.readJsonRelativePath(json, Params.class);
		Sim sim = new Sim(params);
		Recorders recorders = new Recorders();
		SimSetup.setup(sim, recorders);
		SimRun.run(sim, /*verbose*/ true);
		recorders.closeAll();
		double[] results = new double[sim.getMachine().getNumItems()];
		int i = 0;
		for (Item item : sim.getMachine()){
			results[i] = sim.getMetrics().getAverageSurplusMetrics().getAverageSurplusDeviation(item);
		}
		return results;
		
	}
		
}


