package policies;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.apachecommons.CommonsLog;
import lowerbounds.MakeToOrderLowerBound;
import sim.Sim;
import system.Item;
import discreteEvent.ControlEvent;
import discreteEvent.SurplusControlEvent;

@CommonsLog
public class IdealDeviationAndFrequencyTrackingPolicy extends AbstractPolicy {
	
	private double freqTrackingThreshold;
	private MakeToOrderLowerBound makeToOrderLowerBound;	
	
	@Override
	public void setUpPolicy(Sim sim){
		super.setUpPolicy(sim);
		makeToOrderLowerBound = sim.getMakeToOrderLowerBound();
		freqTrackingThreshold = sim.getParams().getPolicyParams().getFreqTrackingThreshold();
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
				
		//Compute the deviation ratios
		Map<Item, Double> deviationRatios = new HashMap<Item, Double>();
		for (Item item : machine){			
			if (item.equals(machine.getSetup())) {
				continue;
			}						
			//Compute the deviation ratio (see Section 2.4.3 of Tubilla 2011)
			double idealDeviation = makeToOrderLowerBound.getIdealSurplusDeviation(item.getId());
			double deviationRatio = (item.getSurplusDeviation() + item.getSetupTime()*item.getDemandRate()) / idealDeviation;
			deviationRatios.put(item, deviationRatio);
		}
		
		//Determine if we should do frequency matching or deviation matching
		boolean doFreqMatching = true;
		for (Item item : machine) {
			if (item.equals(machine.getSetup())) {
				continue;
			}			
			if ( Math.abs( deviationRatios.get(item) - 1.0 ) > freqTrackingThreshold ||
					machine.getLastSetupTime(item) == null) {
				//Only match frequency if all items are within the threshold and we have produced all items at least once
				doFreqMatching = false;
				break;
			}
		}
		
		//Find the next item
		Item nextItem = null;
		if (doFreqMatching) {
			log.trace("Doing frequency matching");
			double largestFrequencyRatio = 0.0;
			for (Item item : machine){
				if (item.equals(machine.getSetup())) {
					continue;
				}	
				double timeBetweenRuns = clock.getTime() - machine.getLastSetupTime(item);
				double idealTimeBetweenRuns = 1.0 / makeToOrderLowerBound.getIdealFrequency(item.getId());
				double frequencyRatio = timeBetweenRuns / idealTimeBetweenRuns;
				if (frequencyRatio > largestFrequencyRatio){
					largestFrequencyRatio = frequencyRatio;
					nextItem = item;
				}
			}
		} else {
			log.trace("Doing deviation matching");
			double largestDeviationRatio = 0.0;
			for (Item item : machine){
				if (item.equals(machine.getSetup())) {
					continue;
				}	
				double deviationRatio = deviationRatios.get(item);
				if (deviationRatio > largestDeviationRatio){
					largestDeviationRatio = deviationRatio;
					nextItem = item;
				}
			}
		}
		
		if (nextItem != null){
			//Most likely scenario
			log.trace("Next item to produce is " + nextItem);
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


