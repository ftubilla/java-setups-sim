package policies;

import lombok.extern.apachecommons.CommonsLog;
import lowerbounds.SurplusCostLowerBound;
import sim.Sim;
import system.Item;
import discreteEvent.ControlEvent;
import discreteEvent.SurplusControlEvent;

@CommonsLog
public class LanAndOlsenPolicy extends AbstractPolicy {

	private SurplusCostLowerBound surplusCostLowerBound;
	
	@Override
	public void setUpPolicy(Sim sim){
		super.setUpPolicy(sim);
		surplusCostLowerBound = sim.getSurplusCostLowerBound();
	}
	
	@Override
	public boolean isTargetBased() {
		return true;
	}

	@Override
	protected ControlEvent onReady() {
		machine.setSprint();
		return new SurplusControlEvent(currentSetup, currentSetup.getSurplusTarget(), clock.getTime(), hasDiscreteMaterial);
	}

	@Override
	protected boolean isTimeToChangeOver() {
		return machine.getSetup().onOrAboveTarget();
	}

	@Override
	protected Item nextItem() {
		
		if (!isTimeToChangeOver()){
			return null;
		}
		
		double largestDeviationRatio = 0.0;
		Item nextItem = null;
		
		for (Item item : machine){
			
			if (item.equals(machine.getSetup())) {
				continue;
			}
			
			//Compute the deviation ratio (see Section 2.4.3 of Tubilla 2011)
			double idealDeviation = surplusCostLowerBound.getIdealSurplusDeviation(item.getId());
			double deviationRatio = (item.getSurplusDeviation() + item.getSetupTime()*item.getDemandRate()) / idealDeviation;
			
			if (deviationRatio > largestDeviationRatio){
				largestDeviationRatio = deviationRatio;
				nextItem = item;
			}
			log.trace(item + " has a deviation ratio of " + deviationRatio);
		}
		
		
		if (nextItem != null){
			//Most likely scenario
			log.trace("Next item to produce is " + nextItem + " which has the largest surplus deviation");
		} else {
			//If all items have the same deviation ratio, find the next item that's not the current setup		
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

}


