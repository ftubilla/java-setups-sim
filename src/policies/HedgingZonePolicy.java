package policies;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import system.Item;

public class HedgingZonePolicy extends AbstractPolicy {
	private static Logger logger = Logger.getLogger(HedgingZonePolicy.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();
	private boolean trace = logger.isTraceEnabled();

		
	@Override
	protected void machineReady() {
		
		if (machine.getSetup().onOrAboveTarget()) {
			startChangeover(nextItem());
		} else {
			double workRemaining = machine.getSetup().minPossibleWorkToTarget(demandProcess);
			machine.setSprint();
			updateControlAfter(workRemaining);
		}
		
	}
	
//	private boolean isInTheHedgingZone(){
//		for (Item item : machine){
//			if (item.getSurplusDeviation() > policyParams.getHedgingThreshold(item)){
//				return false;
//			}
//		}
//		return true;
//	}
	
	private Item nextItem() {
		
		Set<Item> readyItems = new HashSet<Item>();
		double maxRatio = -1;
		Item maximizingItem = null;

		//Add ready items
		for (Item item : machine){
			if (item.getSurplusDeviation() > policyParams.getHedgingThreshold(item)) {
				if (trace) {logger.trace(item + " has deviation " + item.getSurplusDeviation() + " and thus is ready");}
				readyItems.add(item);
			}
			//For breaking up ties later
			double ratio = item.getSurplusDeviation()/(1.0*policyParams.getHedgingThreshold(item));
			if (ratio > maxRatio){
				maxRatio = ratio;
				maximizingItem = item;
			}
		}
		
		//TODO right now we assume that items are sorted by priority and no tie-breaker here
		if (!readyItems.isEmpty()) {
			for (Item item : machine) {			
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
		
}


