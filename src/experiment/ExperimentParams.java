package experiment;

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonProperty;


public class ExperimentParams {
	private static Logger logger = Logger.getLogger(ExperimentParams.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();

	@SuppressWarnings("unused")
	private boolean trace = logger.isTraceEnabled();
	
	@JsonProperty private String name;
	@JsonProperty private int numItems;
	@JsonProperty private NormalizedParameterComboGenerator demandRates;
	@JsonProperty private int replicates;
	
	public String getName(){
		return name;
	}
	
	public Iterable<ParameterCombo> getDemandRateCombos(){
		demandRates.generate(numItems);
		return demandRates.getParameterCombos();
	}
	
	public int getNumItems(){
		return numItems;
	}
	
	public int getReplicates(){
		return replicates;
	}
	
}


