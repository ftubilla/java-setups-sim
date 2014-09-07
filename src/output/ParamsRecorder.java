package output;

import org.apache.log4j.Logger;

import params.Params;

import sim.Sim;

public class ParamsRecorder extends Recorder {
	
	private static Logger logger = Logger.getLogger(ParamsRecorder.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();

	@SuppressWarnings("unused")
	private boolean trace = logger.isTraceEnabled();
	
	public enum Column {SIM_ID, PARAMETER, ITEM, VALUE};
	
	public ParamsRecorder() {
		super("output/params.txt");
		super.writeHeader(Column.class);
	}

	public void recordEndOfSim(Sim sim){
				
		Params params = sim.getParams();
		int numItems = params.getNumItems();
		
		Object[] row = new Object[Column.values().length];
		row[0] = sim.getId();
		
		//Number of items
		row[1] = "NUM_ITEMS";
		row[2] = "NA";
		row[3] = params.getNumItems();
		record(row);
		
		//Generator seeds
		row[1] = "SEED";
		row[2] = "NA";
		row[3] = params.getSeed();
		record(row);
		
		//Demand rates
		row[1] = "DEMAND_RATES";
		for (int i=0; i<numItems; i++){
			row[2] = i;
			row[3] = params.getDemandRates().get(i);
			record(row);
		}
		
		//Setup times
		row[1] = "SETUP_TIMES";
		for (int i=0; i<numItems; i++){
			row[2] = i;
			row[3] = params.getSetupTimes().get(i);
			record(row);
		}
		
		//Policy
		row[1] = "POLICY";
		row[2] = "NA";
		row[3] = params.getPolicyParams().getName();
		record(row);
		
	}
	
	
}


