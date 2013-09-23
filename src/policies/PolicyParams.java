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
	
	private boolean isLocked = false;
	
	public String getName(){
		return this.name;
	}
	
	public double getHedgingThresholdDifference(Item item){
		return item.getSurplusTarget()-lowerHedgingPoints.get(item.getId());
	}

	public double getLowerHedgingPoint(Item item) {
		return lowerHedgingPoints.get(item.getId());
	}
	
	public String getPriorityComparator(){
		return priorityComparator;
	}
	
	public boolean shouldCruise(){
		return shouldCruise;
	}
	
	public void lock(){
		logger.trace("Locking policy params");
		isLocked = true;
	}
	
		
	//Setters
	public void setName(String name){
		assert !isLocked : "The params are locked!";
		this.name = name;
	}
	
	public void setLowerHedgingPoint(Item item, Double value){
		assert !isLocked : "The params are locked!";
		lowerHedgingPoints.add(item.getId(), value);
	}
	
	public void setPriorityComparator(String priorityComparator){
		assert !isLocked : "The params are locked!";
		this.priorityComparator = priorityComparator;
	}
	
	public void setShouldCruise(boolean shouldCruise){
		assert !isLocked : "The params are locked!";
		this.shouldCruise = shouldCruise; 
	}
	
}


