package policies;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.apachecommons.CommonsLog;
import lowerbounds.SurplusCostLowerBound;
import sim.Sim;
import system.Item;
import discreteEvent.ControlEvent;
import discreteEvent.SurplusControlEvent;

/**
 * Selects the item that's farthest behind on its ideal frequency of setups,
 * using a learning average to compute the frequency.
 *  
 * @author ftubilla
 *
 */
@Deprecated
@CommonsLog
public class IdealFrequencyTrackingPolicy extends AbstractPolicy {

	private double learningRate;
	private Map<Item, Double> aveTimeBetweenRuns;
	private SurplusCostLowerBound makeToOrderLowerBound;	
	
	@Override
	public void setUpPolicy(Sim sim){
		super.setUpPolicy(sim);
		makeToOrderLowerBound = sim.getSurplusCostLowerBound();
		this.learningRate = sim.getParams().getPolicyParams().getLearningRate();
		this.aveTimeBetweenRuns = new HashMap<Item, Double>();
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
			if (aveTimeBetweenRuns.get(item) == null){
				//Don't do frequency matching until all items have a frequency estimate
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
				double idealTimeBetweenRuns = 1.0 / makeToOrderLowerBound.getIdealFrequency(item.getId());
				
				double aveTimeBetweenRunsIfProduced = computeAveTimeBetweenRuns(item);
				
				double frequencyRatio = aveTimeBetweenRunsIfProduced / idealTimeBetweenRuns;
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
		
		//Update the learned time between runs (TODO: this call makes the method no longer idempotent)
		if (!aveTimeBetweenRuns.containsKey(nextItem)){
			aveTimeBetweenRuns.put(nextItem, 0.0);
		}
		
		if (machine.getLastSetupTime(nextItem) != null){
			//Only start tracking the average after producing the item for the first time		
			aveTimeBetweenRuns.put(nextItem, computeAveTimeBetweenRuns(nextItem));
		}
		
		return nextItem;
	}


	private double computeAveTimeBetweenRuns(Item item){
		double timeBetweenRunsIfProduced = clock.getTime().subtract(machine.getLastSetupTime(item)).doubleValue();
		return (1-learningRate) * aveTimeBetweenRuns.get(item) + learningRate * timeBetweenRunsIfProduced;		
	}
	
}


