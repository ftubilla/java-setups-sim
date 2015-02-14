package policies;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.apachecommons.CommonsLog;
import lowerbounds.MakeToOrderLowerBound;
import sim.Sim;
import system.Item;
import discreteEvent.ControlEvent;
import discreteEvent.SurplusControlEvent;

/**
 * Minimizes error from both the ideal frequency and the ideal
 * surplus deviation, by using a weighted combination of the two.
 *  
 * @author ftubilla
 *
 */
@CommonsLog
public class IdealDeviationAndFrequencyTrackingPolicy extends AbstractPolicy {

	private double learningRate;
	private double deviationTrackingBias;
	private Map<Item, Double> aveTimeBetweenRuns;
	private Map<Item, Double> aveMaxDeviation;
	private MakeToOrderLowerBound makeToOrderLowerBound;	
	
	@Override
	public void setUpPolicy(Sim sim){
		super.setUpPolicy(sim);
		makeToOrderLowerBound = sim.getMakeToOrderLowerBound();
		this.learningRate = sim.getParams().getPolicyParams().getLearningRate();
		this.deviationTrackingBias = sim.getParams().getPolicyParams().getDeviationTrackingBias();
		this.aveTimeBetweenRuns = new HashMap<Item, Double>();
		this.aveMaxDeviation = new HashMap<Item, Double>();
		//Initialize the structures
		for (Item item : sim.getMachine()) {
			this.aveTimeBetweenRuns.put(item, 0.0);
			this.aveMaxDeviation.put(item, 0.0);
		}
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
		
		Item nextItem = null;
		double largestErrorRatio = 0.0;		
		
		for (Item item : machine){
			
			if (item.equals(machine.getSetup())){
				continue;
			}
			
			//First compute the deviation ratio
			double idealDeviation = makeToOrderLowerBound.getIdealSurplusDeviation(item.getId());
			double deviationRatioIfProduced = computeAveMaxDeviation(item);
			double deviationErrorRatio = deviationRatioIfProduced / idealDeviation;
			
			//Now the Frequency Ratio (assuming the item has been produced at least once)
			double invFreqErrorRatio = 0.0;
			if (machine.getLastSetupTime(item) != null) {
				double idealTimeBetweenRuns = 1.0 / makeToOrderLowerBound.getIdealFrequency(item.getId());			
				double aveTimeBetweenRunsIfProduced = computeAveTimeBetweenRuns(item);			
				invFreqErrorRatio = aveTimeBetweenRunsIfProduced / idealTimeBetweenRuns;
			}
			
			//Get the largest of the two and see if this items has the biggest error ratio
			double itemErrorRatio = deviationErrorRatio * deviationTrackingBias + invFreqErrorRatio * ( 1 - deviationTrackingBias );
			if (itemErrorRatio > largestErrorRatio) {
				nextItem = item;
				largestErrorRatio = itemErrorRatio;
			}
			
		}

		if (nextItem != null){
			//Most likely scenario
			log.trace("Next item to produce is " + nextItem);
		} else {
			//If all items have the same error ratio, find the next item that's not the current setup		
			for (Item item : machine){
				if (item != machine.getSetup()){
					nextItem = item;
					log.trace("All items had the same error ratio. Changing over to the next item " + nextItem);
					break;
				}
			}			
		}
		
		//Update the learned values (TODO: this call makes the method no longer idempotent)
		if (machine.getLastSetupTime(nextItem) != null){
			aveTimeBetweenRuns.put(nextItem, computeAveTimeBetweenRuns(nextItem));
		}
		aveMaxDeviation.put(nextItem, computeAveMaxDeviation(nextItem));
		
		return nextItem;
	}


	private double computeAveTimeBetweenRuns(Item item){
		double timeBetweenRunsIfProduced = clock.getTime() - machine.getLastSetupTime(item);
		return (1-learningRate) * aveTimeBetweenRuns.get(item) + learningRate * timeBetweenRunsIfProduced;		
	}
	
	private double computeAveMaxDeviation(Item item){
		double maxDeviationIfProduced = item.getSurplusDeviation() + item.getSetupTime()*item.getDemandRate();
		return (1-learningRate) * aveMaxDeviation.get(item) + learningRate * maxDeviationIfProduced;
	}
	
}


