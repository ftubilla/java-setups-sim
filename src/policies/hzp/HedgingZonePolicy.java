package policies.hzp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import policies.AbstractPolicy;
import sim.AlgorithmLoader;
import sim.Sim;
import system.Item;

public class HedgingZonePolicy extends AbstractPolicy {
	private static Logger logger = Logger.getLogger(HedgingZonePolicy.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();
	private boolean trace = logger.isTraceEnabled();
		
	private IPriorityComparator priorityComparator;
	private List<Item> sortedItems;
	
	@Override
	public void setUpPolicy(Sim sim){
		super.setUpPolicy(sim);
		priorityComparator = AlgorithmLoader.load("policies", policyParams.getPriorityComparator(),IPriorityComparator.class);
		//Note that in general the sortation can be dynamic
		sortedItems = new ArrayList<Item>(machine.getNumItems());
		for (Item item : machine){
			sortedItems.add(item);
		}
	}
	
	@Override
	protected boolean isTimeToChangeOver() {
		if (policyParams.shouldCruise() && isInTheHedgingZone()){
			//Changeovers never occur in the hedging zone
			return false;
		} else {
			return machine.getSetup().onOrAboveTarget();
		}
	}
	
	@Override
	protected double doUntilNextUpdate() {	
		if (policyParams.shouldCruise() && isInTheHedgingZone() && machine.getSetup().onTarget()){
			//Only cruise when 1) it's enabled, 2) we are in the hedging zone, 3) we are at the target ZU
			machine.setCruise();
			return computeTimeToExitHedgingZone();			
		} else {
			machine.setSprint();
			return machine.getSetup().computeMinDeltaTimeToTarget(productionProcess, demandProcess);
		}
	}
	
	private boolean isInTheHedgingZone(){
		for (Item item : sortedItems){
			//Mathematically, we want strict inequality here, but here we need to use nonstrict to avoid getting stucked
			if (item.getSurplusDeviation() >= policyParams.getHedgingThresholdDifference(item)){
				return false;
			}
		}
		return true;
	}
	
	private double computeTimeToExitHedgingZone(){
		double minExitTime = Double.MAX_VALUE;
		for (Item item : sortedItems){
			double exitTime = item.computeMinDeltaTimeToSurplusLevel(policyParams.getLowerHedgingPoint(item), productionProcess, demandProcess);
			assert exitTime >= 0 : "The system is not in the hedging zone!";
			if (exitTime < minExitTime){minExitTime = exitTime;}
		}
		return minExitTime;
	}
	
	@Override
	protected Item nextItem() {

		//Sort items
		Collections.sort(sortedItems, priorityComparator);
		if (trace) {
			logger.trace("Sorting items by priority:");
			for (Item item : sortedItems){
				logger.trace("Next priority " + item);
			}
		}
				
		Set<Item> readyItems = new HashSet<Item>();
		double maxRatio = -1;
		Item maximizingItem = null;

		//Add ready items
		for (Item item : sortedItems){
			if (item.getSurplusDeviation() > policyParams.getHedgingThresholdDifference(item)) {
				if (trace) {logger.trace(item + " has deviation " + item.getSurplusDeviation() + " and thus is ready");}
				readyItems.add(item);
			}
			//For breaking up ties later
			double ratio = item.getSurplusDeviation()/(1.0*policyParams.getHedgingThresholdDifference(item));
			if (ratio > maxRatio){
				maxRatio = ratio;
				maximizingItem = item;
			}
		}
		
		if (!readyItems.isEmpty()) {
			for (Item item : sortedItems) {			
				if (readyItems.contains(item)){
					if (trace){logger.trace(item + " has the highest priority and is ready.");}
					return item;				
				}			
			}
		} else {
			if (trace){logger.trace("The system is in the hedging zone. Returning " + maximizingItem + 
					" with maximizing surplus dev ratio " + maxRatio);}
			return maximizingItem;
		}
		
		return null;
	}

	@Override
	public boolean isTargetBased() {
		return true;
	}
	
	
		
}


