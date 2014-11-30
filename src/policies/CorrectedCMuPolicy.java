package policies;

import lombok.extern.apachecommons.CommonsLog;
import policies.tuning.ILowerHedgingPointsComputationMethod;
import sim.Sim;
import system.Item;
import util.AlgorithmLoader;
import discreteEvent.ControlEvent;
import discreteEvent.SurplusControlEvent;

@CommonsLog
public class CorrectedCMuPolicy extends AbstractPolicy {

	private ILowerHedgingPointsComputationMethod lowerHedgingPoints;
	
	@Override
	public void setUpPolicy(Sim sim){
		super.setUpPolicy(sim);
		assert sim.getParams().getMeanTimeToFail() > sim.getParams().getFinalTime() : "Random case not yet implemented";
		//Get the lower hedging points
		lowerHedgingPoints = AlgorithmLoader.load("policies.tuning", 
				policyParams.getLowerHedgingPointsComputationMethod(), 
				ILowerHedgingPointsComputationMethod.class);
		lowerHedgingPoints.compute(sim);
	}	
	
	@Override
	public boolean isTargetBased() {
		return true;
	}

	@Override
	protected ControlEvent onReady() {
		machine.setSprint();
		//On each review period, we want to bring a change in the surplus of surplusLevelDelta
		double surplusDelta = computeSurplusDelta(currentSetup);
		return new SurplusControlEvent(currentSetup,surplusDelta + currentSetup.getSurplus(),clock.getTime(),
				hasDiscreteMaterial);
	}

	@Override
	protected boolean isTimeToChangeOver() {
		return !(nextItem()==currentSetup);
	}

	@Override
	protected Item nextItem() {
		
		//Find the item with the highest c \tilde{mu}
		//TODO this only works if the inventory/backlog costs are proportional		
		Item nextItem=null;
		double maxCMu=-1;
		for (Item item : machine){
			if (item.onOrAboveTarget()){
				if(log.isTraceEnabled()){
					log.trace("Skipping " + item + " because it is at its target already");
				}
				continue;
			}
			double surplusDelta = computeSurplusDelta(item);			
			double timeToReach = item.getFluidTimeToSurplusLevel(surplusDelta + currentSetup.getSurplus());
			if (!item.isUnderProduction()){
				timeToReach += item.getSetupTime();
			}
			double averageMu = surplusDelta/timeToReach + item.getDemandRate();
			double cMu = item.getBacklogCostRate()*averageMu;
			if(log.isTraceEnabled()){log.trace("Average cmu for " + item + " is " + cMu);}
			if (cMu > maxCMu){
				maxCMu = cMu;
				nextItem = item;
			}
		}
		if(log.isTraceEnabled()){log.trace("Maximizing cmu is " + nextItem);}
		return nextItem;
	}
	
	protected double computeSurplusDelta(Item item){
		double surplusLevelDelta = item.getSurplusTarget() - lowerHedgingPoints.getLowerHedgingPoint(item);  
		if(log.isTraceEnabled()){
			log.trace("If producing " + item + " we would like a Delta suplus of " + surplusLevelDelta);
		}
		if (surplusLevelDelta + item.getSurplus() > item.getSurplusTarget()){
			surplusLevelDelta = item.getSurplusTarget() - item.getSurplus();
			if (log.isTraceEnabled()){
				log.trace("Truncating delta in surplus to " + surplusLevelDelta + " so that target is not exceeded");
			}
		}
		assert surplusLevelDelta >= -1.0 : "Surplus delta cannot be " + surplusLevelDelta;
		return surplusLevelDelta;
	}
}


