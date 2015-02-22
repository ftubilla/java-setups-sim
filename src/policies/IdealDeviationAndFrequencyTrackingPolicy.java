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
	private Map<Item, Double> cumTargetToTargetDevArea;
	private Map<Item, Double> cumTargetToTargetTime;
	private MakeToOrderLowerBound makeToOrderLowerBound;	
	private enum Update {TRUE, FALSE};
	
	@Override
	public void setUpPolicy(Sim sim){
		super.setUpPolicy(sim);
		makeToOrderLowerBound = sim.getMakeToOrderLowerBound();
		this.learningRate = sim.getParams().getPolicyParams().getLearningRate();
		this.deviationTrackingBias = sim.getParams().getPolicyParams().getDeviationTrackingBias();
		this.aveTimeBetweenRuns = new HashMap<Item, Double>();
		this.cumTargetToTargetDevArea = new HashMap<Item, Double>();
		this.cumTargetToTargetTime = new HashMap<Item, Double>();
		//Initialize the structures
		for (Item item : sim.getMachine()) {
			this.aveTimeBetweenRuns.put(item, 0.0);
			this.cumTargetToTargetDevArea.put(item, 0.0);
			this.cumTargetToTargetTime.put(item, 0.0);
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
			double deviationRatioIfProduced = computeAveMaxDeviation(item, Update.FALSE);
			double deviationErrorRatio = deviationRatioIfProduced / idealDeviation;
			
			//Now the Frequency Ratio (assuming the item has been produced at least once)
			double invFreqErrorRatio = 0.0;
			if (machine.getLastSetupTime(item) != null) {
				double idealTimeBetweenRuns = 1.0 / makeToOrderLowerBound.getIdealFrequency(item.getId());			
				double aveTimeBetweenRunsIfProduced = computeAveTimeBetweenRuns(item, Update.FALSE);			
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
			aveTimeBetweenRuns.put(nextItem, computeAveTimeBetweenRuns(nextItem, Update.TRUE));
		}
		computeAveMaxDeviation(nextItem, Update.TRUE);
			
		return nextItem;
	}


	private double computeAveTimeBetweenRuns(Item item, Update update){
		double timeBetweenRunsIfProduced = clock.getTime() - machine.getLastSetupTime(item);
		double aveTime = (1-learningRate) * aveTimeBetweenRuns.get(item) + learningRate * timeBetweenRunsIfProduced;
		if (update == Update.TRUE) {
			aveTimeBetweenRuns.put(item, aveTime);
		}
		return aveTime;
 	}
	
	private double targetToTargetTime(double deviation, double demandRate, double productionRate){	
		//TODO Correct for machine efficiency!!!
		double rho = demandRate / productionRate;
		return deviation / demandRate / ( 1 - rho );
	}
		
	private double computeAveMaxDeviation(Item item, Update update){
		//Computes a squared weighted average. We essentially add the area of each triangle formed between successive runs of an item
		//and divide by the time it took between successive runs (cycle time); this is the base of the triangle
		double maxDeviationIfProduced = item.getSurplusDeviation() + item.getSetupTime() * item.getDemandRate();		
		
		double targetToTargetTime = targetToTargetTime(maxDeviationIfProduced, item.getDemandRate(), item.getProductionRate());
		double devTriangleArea = 0.5 * maxDeviationIfProduced * targetToTargetTime;
		
		//The factor of 2 is because we want the highest point in the deviation triangle, not the average deviation
		double aveMaxDev = 2 * ( cumTargetToTargetDevArea.get(item) + devTriangleArea ) / ( cumTargetToTargetTime.get(item) + targetToTargetTime );

		if (update == Update.TRUE) {
			cumTargetToTargetDevArea.put(item, cumTargetToTargetDevArea.get(item) + devTriangleArea);
			cumTargetToTargetTime.put(item, cumTargetToTargetTime.get(item) + targetToTargetTime);
		}
		
		//Multiply by 2 because we want the max deviation
		return  (1 - learningRate) * aveMaxDev + learningRate * maxDeviationIfProduced;
	}
	
}


