package output;

import params.Params;
import sim.Sim;

public class ParamsRecorder extends Recorder {
	
	public enum Column {SIM_ID, PARAMETER, ITEM, VALUE};
	
	public ParamsRecorder() {
		super("output/params.txt");
		super.writeHeader(Column.class);
	}

	public void recordEndOfSim(Sim sim){
				
		Params params = sim.getParams();
		for (String[] triad : params.getValueTriads()) {
			record(String.format("%d %s %s %s", sim.getId(), triad[0], triad[1], triad[2]));
		}
		
	}
	
	
}


