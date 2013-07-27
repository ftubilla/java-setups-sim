package policies;

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonProperty;

import sim.IParams;

public class PolicyParams implements IParams{
	
	private static Logger logger = Logger.getLogger(PolicyParams.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();

	@SuppressWarnings("unused")
	private boolean trace = logger.isTraceEnabled();
	
	@JsonProperty private String name;
	
	public String getName(){
		return this.name;
	}
	
}


