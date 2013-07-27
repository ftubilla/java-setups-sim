package processes.demand;

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonProperty;

import sim.IParams;

public class DemandProcessParams implements IParams {
	
	private static Logger logger = Logger.getLogger(DemandProcessParams.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();

	@SuppressWarnings("unused")
	private boolean trace = logger.isTraceEnabled();
	
	@JsonProperty private String name;
	@JsonProperty public int demandBatchSize;
	
	public String getName(){
		return this.name;
	}
	
	public int getDemandBatchSize(){
		return this.demandBatchSize;
	}
	
}


