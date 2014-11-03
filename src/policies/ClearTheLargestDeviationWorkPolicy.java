package policies;

import org.apache.log4j.Logger;

import system.Item;
import discreteEvent.ControlEvent;
import discreteEvent.SurplusControlEvent;

public class ClearTheLargestDeviationWorkPolicy extends AbstractPolicy {
	private static Logger logger = Logger.getLogger(ClearTheLargestDeviationWorkPolicy.class);

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
		
		double largestDeviationWork = 0.0;
		Item nextItem = null;
		
		for (Item item : machine){
			double workToClearDeviation = item.getFluidTimeToSurplusLevel(item.getSurplusTarget());
			if (workToClearDeviation > largestDeviationWork){
				largestDeviationWork = workToClearDeviation;
				nextItem = item;
			}
			if (trace){logger.trace(String.format("Item %d needs %.3f work time to clear its deviation",
					item.getId(), workToClearDeviation));}
		}
		
		
		if (nextItem != null){
			//Most likely scenario
			if (trace){logger.trace("Next item to produce is " + nextItem + " which has the largest surplus deviation work time");}
		} else {
			//If all items have the same deviation cost, find the next item that's not the current setup		
			for (Item item : machine){
				if (item != machine.getSetup()){
					nextItem = item;
					if (trace){logger.trace("All items had the same surplus deviation work time. Changing over to the next item " + nextItem);}
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


