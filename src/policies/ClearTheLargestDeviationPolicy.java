package policies;

import org.apache.log4j.Logger;

import system.Item;
import discreteEvent.ControlEvent;
import discreteEvent.SurplusControlEvent;

public class ClearTheLargestDeviationPolicy extends AbstractPolicy {
	private static Logger logger = Logger.getLogger(ClearTheLargestDeviationPolicy.class);

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
		return new SurplusControlEvent(currentSetup, currentSetup.getSurplusTarget(),
				clock.getTime(),productionProcess,demandProcess);
	}

	@Override
	protected Item nextItem() {
		
		double largestDeviation = 0.0;
		Item nextItem = null;
		
		for (Item item : machine){
			if (item.getSurplusDeviation() > largestDeviation){
				largestDeviation = item.getSurplusDeviation();
				nextItem = item;
			}
			if (trace){logger.trace(item + " has a surplus deviation of " + item.getSurplusDeviation());}
		}
		
		
		if (nextItem != null){
			//Most likely scenario
			if (trace){logger.trace("Next item to produce is " + nextItem + " which has the largest surplus deviation");}
		} else {
			//If all items have the same deviation, find the next item that's not the current setup		
			for (Item item : machine){
				if (item != machine.getSetup()){
					nextItem = item;
					if (trace){logger.trace("All items had the same surplus deviation. Changing over to the next item " + nextItem);}
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


