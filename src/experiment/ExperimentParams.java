package experiment;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;


public class ExperimentParams {
	private static Logger logger = Logger.getLogger(ExperimentParams.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();

	@SuppressWarnings("unused")
	private boolean trace = logger.isTraceEnabled();
	
	@JsonProperty private String name;
	@JsonProperty private int numThreads;
	@JsonProperty private String baseJson;
	@JsonProperty private int numItems;
	@JsonProperty private IParameterComboGenerator demandRates;
	@JsonProperty private IParameterComboGenerator setupTimes;	
	@JsonProperty private int replicates;
	
	public String getName(){
		return name;
	}
	
	public String getBaseJson(){
		return baseJson;
	}
	
	public int getNumThreads(){
		return numThreads;
	}
	
	public Iterable<ParameterCombo> getDemandRatesCombos(){
		demandRates.generate(numItems);
		return demandRates.getParameterCombos();
	}
	
	public Iterable<ParameterCombo> getSetupTimesCombos(){
		setupTimes.generate(numItems);
		return setupTimes.getParameterCombos();
	}
	
	public int getNumItems(){
		return numItems;
	}
	
	public int getReplicates(){
		return replicates;
	}
	
}


