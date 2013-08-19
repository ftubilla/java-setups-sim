package policies;

import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonProperty;

import sim.IParams;
import system.Item;

public class PolicyParams implements IParams{
	
	private static Logger logger = Logger.getLogger(PolicyParams.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();

	@SuppressWarnings("unused")
	private boolean trace = logger.isTraceEnabled();
	
	@JsonProperty private String name;
	@JsonProperty private List<Double> lowerHedgingPoints;
	
	//Hedging Zone Policy Params
	@JsonProperty private String priorityComparator="hzp.CMuComparatorWithTiesById";
	@JsonProperty private boolean shouldCruise=false;
	
	public String getName(){
		return this.name;
	}
	
	public double getHedgingThresholdDifference(Item item){
		return item.getSurplusTarget()-lowerHedgingPoints.get(item.getId());
	}
	
	public String getPriorityComparator(){
		return priorityComparator;
	}
	
	public boolean shouldCruise(){
		return shouldCruise;
	}
	
}


