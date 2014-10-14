package policies;

import lombok.extern.apachecommons.CommonsLog;
import system.Item;
import discreteEvent.ControlEvent;
import discreteEvent.SurplusControlEvent;

@CommonsLog
public class ClearTheLargestDeviationPolicy extends AbstractPolicy {

	@Override
	protected boolean isTimeToChangeOver() {
		return machine.getSetup().onOrAboveTarget();
	}

	@Override
	protected ControlEvent onReady() {
		machine.setSprint();
		return new SurplusControlEvent(currentSetup, currentSetup.getSurplusTarget(), clock.getTime(), hasDiscreteMaterial);
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
			log.trace(item + " has a surplus deviation of " + item.getSurplusDeviation());
		}
		
		
		if (nextItem != null){
			//Most likely scenario
			log.trace("Next item to produce is " + nextItem + " which has the largest surplus deviation");
		} else {
			//If all items have the same deviation, find the next item that's not the current setup		
			for (Item item : machine){
				if (item != machine.getSetup()){
					nextItem = item;
					log.trace("All items had the same surplus deviation. Changing over to the next item " + nextItem);
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


