package policies;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.apachecommons.CommonsLog;
import lowerbounds.SurplusCostLowerBound;
import sim.Sim;
import system.Item;
import util.containers.FixedHorizonSurplusTrajectoryContainer;
import util.containers.ISurplusTrajectoryContainer;
import discreteEvent.ControlEvent;
import discreteEvent.Event;
import discreteEvent.EventListener;
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
	private ISurplusTrajectoryContainer pastSurplus;
	private SurplusCostLowerBound makeToOrderLowerBound;	
	private enum Update {TRUE, FALSE};
	
	private int SURPLUS_LOOKBACK_TIME = 500;
	
	@Override
	public void setUpPolicy(Sim sim){
		super.setUpPolicy(sim);
		makeToOrderLowerBound = sim.getSurplusCostLowerBound();
		this.learningRate = sim.getParams().getPolicyParams().getLearningRate();
		this.deviationTrackingBias = sim.getParams().getPolicyParams().getDeviationTrackingBias();
		this.aveTimeBetweenRuns = new HashMap<Item, Double>();
		this.pastSurplus = new FixedHorizonSurplusTrajectoryContainer(SURPLUS_LOOKBACK_TIME, sim.getParams().getSurplusTargets()); 
		//Initialize the structures
		for (Item item : sim.getMachine()) {
			this.aveTimeBetweenRuns.put(item, 0.0);
		}
		
		//Add an event listener to make sure the surplus trajectory container is in sync
		sim.getListenersCoordinator().addBeforeEventListener(new EventListener() {			
			@Override
			public void execute(Event event, Sim sim) {
				double time = event.getTime();
				double surplus[] = new double[machine.getNumItems()];
				for (int i=0; i < surplus.length; i++) {
					surplus[i] = machine.getItemById(i).getSurplus();
				}
				pastSurplus.addPoint(time, surplus);
			}
		});
		
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
			double deviationIfProduced = computeAveMaxDeviation(item);
			double deviationErrorRatio = deviationIfProduced / idealDeviation;
			
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
			if (log.isTraceEnabled()){
				for (Item item : machine) {
					boolean isNext = nextItem.equals(item);
					log.trace(String.format("time,item,is_next,deviation,deviation_if_produced,ideal_deviation," +
							"%.6f,%s,%s,%.3f,%.3f,%.3f",
							clock.getTime(), item.getId(), isNext, 
							item.getSurplusDeviation(), computeAveMaxDeviation(item), makeToOrderLowerBound.getIdealSurplusDeviation(item.getId())));

				}
			}
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
			
	private double computeAveMaxDeviation(Item item){

		//First determine the dev at the start of the next run for this item, if we were to produce it next
		double maxDeviationIfProduced = item.getSurplusDeviation() + item.getSetupTime() * item.getDemandRate();		
		
		//Now compute the average max deviation using this point and the prev history
		ISurplusTrajectoryContainer surplusTraj = pastSurplus.copy();
		double[] newSurplusPoint = new double[machine.getNumItems()];
		//We only care about the current item for this calculation, so we can ignore the surplus values for the others
		newSurplusPoint[item.getId()] = item.getSurplusTarget() - maxDeviationIfProduced; 	
		surplusTraj.addPoint(clock.getTime() + item.getSetupTime(), newSurplusPoint);
				
		//The factor of 2 is because we want the highest point in the deviation triangle, not the average deviation
		double pastSurplusArea = surplusTraj.getSurplusDeviationArea()[item.getId()];
		double pastSurplusTimeRange = surplusTraj.getLatestTime() - surplusTraj.getEarliestTime();
		double aveMaxDev = 2 * pastSurplusArea / pastSurplusTimeRange;

		return aveMaxDev;
	}
	
}


