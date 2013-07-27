package processes.production;

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonProperty;

import sim.IParams;

public class ProductionProcessParams implements IParams {
	private static Logger logger = Logger.getLogger(ProductionProcessParams.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();

	@SuppressWarnings("unused")
	private boolean trace = logger.isTraceEnabled();
	
	@JsonProperty private String name;
	@JsonProperty private int productionBatchSize;

	public String getName(){
		return this.name;
	}
	
	public int getProductionBatchSize(){
		return this.productionBatchSize;
	}
	
}


