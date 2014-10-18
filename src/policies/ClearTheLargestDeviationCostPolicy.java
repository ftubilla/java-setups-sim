package policies;

import org.apache.log4j.Logger;

import system.Item;
import discreteEvent.ControlEvent;
import discreteEvent.SurplusControlEvent;

public class ClearTheLargestDeviationCostPolicy extends AbstractPolicy {
	private static Logger logger = Logger.getLogger(ClearTheLargestDeviationCostPolicy.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();
	private boolean trace = logger.isTraceEnabled();


	@Override
	protected boolean isTimeToChangeOver() {
		return machine.getSetup().onOrAboveTarget();
	}

	@Override
	protected ControlEvent onReady() {
		machine.setSprint();
		return new SurplusControlEvent(currentSetup,currentSetup.getSurplusTarget(),clock.getTime(),hasDiscreteMaterial);
	}

	@Override
	protected Item nextItem() {
		
		if (!isTimeToChangeOver()){
			return null;
		}
		
		double largestDeviationCost = 0.0;
		Item nextItem = null;
		
		for (Item item : machine){
			if (item.getSurplusDeviation()*item.getBacklogCostRate() > largestDeviationCost){
				largestDeviationCost = item.getSurplusDeviation()*item.getBacklogCostRate();
				nextItem = item;
			}
			if (trace){logger.trace(item + " has a surplus deviation Cost of " + item.getSurplusDeviation()*item.getBacklogCostRate());}
		}
		
		
		if (nextItem != null){
			//Most likely scenario
			if (trace){logger.trace("Next item to produce is " + nextItem + " which has the largest surplus deviation cost");}
		} else {
			//If all items have the same deviation cost, find the next item that's not the current setup		
			for (Item item : machine){
				if (item != machine.getSetup()){
					nextItem = item;
					if (trace){logger.trace("All items had the same surplus deviation cost. Changing over to the next item " + nextItem);}
					break;
				}
			}			
		}
		return nextItem;
	}

	@Override
	public boolean isTargetBased() {
		return true;
	}


}


