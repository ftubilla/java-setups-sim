package policies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.apachecommons.CommonsLog;
import policies.tuning.ILowerHedgingPointsComputationMethod;
import policies.tuning.IPriorityComparator;
import sim.Sim;
import system.Item;
import util.AlgorithmLoader;
import discreteEvent.ControlEvent;
import discreteEvent.SurplusControlEvent;

@CommonsLog
public class HedgingZonePolicy extends AbstractPolicy {

	private IPriorityComparator priorityComparator;
	private List<Item> sortedItems;
	private ILowerHedgingPointsComputationMethod lowerHedgingPoints;
	private boolean canCruise;
	
	@Override
	public void setUpPolicy(Sim sim){
		
		super.setUpPolicy(sim);
		
		//Get the item priority static comparator)
		priorityComparator = AlgorithmLoader.load("policies.tuning", policyParams.getPriorityComparator(), IPriorityComparator.class);
		sortedItems = new ArrayList<Item>(machine.getNumItems());
		for (Item item : machine){
			sortedItems.add(item);
		}
		Collections.sort(sortedItems, priorityComparator);
		if (log.isTraceEnabled()) {
			log.trace("Sorting items by priority:");
			for (Item item : sortedItems){
				log.trace("Next priority " + item);
			}
		}
		
		//Get the lower hedging points
		lowerHedgingPoints = AlgorithmLoader.load("policies.tuning", 
				policyParams.getLowerHedgingPointsComputationMethod(), 
				ILowerHedgingPointsComputationMethod.class);
		lowerHedgingPoints.compute(sim);
		
		//TODO For now, we assume the policy never cruises
		canCruise = false;
		if (policyParams.getUserDefinedIsCruising().isPresent() && policyParams.getUserDefinedIsCruising().get()){
			log.warn("Cruising is set to true by the user but right now the policy is non cruising!!!");
		}
	}
	
	@Override
	protected boolean isTimeToChangeOver() {
		if (canCruise && isInTheHedgingZone()){
			//Changeovers never occur in the hedging zone
			return false;
		} else {
			return machine.getSetup().onOrAboveTarget();
		}
	}
	
	@Override
	protected ControlEvent onReady() {	
		if (canCruise && isInTheHedgingZone() && machine.getSetup().onTarget()){
			//Only cruise when 1) it's enabled, 2) we are in the hedging zone, 3) we are at the target ZU
			machine.setCruise();
			return new ControlEvent(clock.getTime()+computeTimeToExitHedgingZone());			
		} else {
			machine.setSprint();
			return new SurplusControlEvent(currentSetup, currentSetup.getSurplusTarget(),
					clock.getTime(), hasDiscreteMaterial);
		}
	}
	
	private boolean isInTheHedgingZone(){
		for (Item item : sortedItems){
			//Mathematically, we want strict inequality here, but here we need to use nonstrict to avoid getting stuck
			if (item.getSurplusDeviation() >= item.getSurplusTarget() - lowerHedgingPoints.getLowerHedgingPoint(item)){
				return false;
			}
		}
		return true;
	}
	
	private double computeTimeToExitHedgingZone(){
		double minExitTime = Double.MAX_VALUE;
		for (Item item : sortedItems){
			double exitTime = item.getFluidTimeToSurplusLevel(lowerHedgingPoints.getLowerHedgingPoint(item));			
			assert exitTime >= 0 : "The system is not in the hedging zone!";
			if (exitTime < minExitTime){minExitTime = exitTime;}
		}
		return minExitTime;
	}
	
	@Override
	protected Item nextItem() {
				
		if (!isTimeToChangeOver()) {
			return null;
		}
		
		Set<Item> readyItems = new HashSet<Item>();
		double maxRatio = -1;
		Item maximizingItem = null;

		//Add ready items
		for (Item item : sortedItems){
			double itemThresholdDiff = item.getSurplusTarget() - lowerHedgingPoints.getLowerHedgingPoint(item);
			if (item.getSurplusDeviation() > itemThresholdDiff) {
				if (log.isTraceEnabled()) {
					log.trace(item + " has deviation " + item.getSurplusDeviation() + " and thus is ready");
				}
				readyItems.add(item);
			}
			//For breaking up ties later
			double ratio = item.getSurplusDeviation() / itemThresholdDiff; 
			if (ratio > maxRatio){
				maxRatio = ratio;
				maximizingItem = item;
			}
		}
		
		if (!readyItems.isEmpty()) {
			for (Item item : sortedItems) {			
				if (readyItems.contains(item)){
					if (log.isTraceEnabled()){
						log.trace(item + " has the highest priority and is ready.");
					}
					return item;				
				}			
			}
		} else {
			if (log.isTraceEnabled()){
				log.trace("The system is in the hedging zone. Returning " + maximizingItem + 
					" with maximizing surplus dev ratio " + maxRatio);
			}
			return maximizingItem;
		}
		
		return null;
	}

	@Override
	public boolean isTargetBased() {
		return true;
	}
		
		
}


