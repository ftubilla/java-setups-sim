package output;

import org.apache.log4j.Logger;

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
		String row = sim.getId() + " seed " + " NA " + sim.getParams().getSeed();
		super.record(row);
	}
	
	
}


