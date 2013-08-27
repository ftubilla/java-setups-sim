package policies;

import org.apache.log4j.Logger;

import sim.Sim;
import system.Item;

public class CorrectedCMuPolicy extends AbstractPolicy {
	private static Logger logger = Logger.getLogger(CorrectedCMuPolicy.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();
	private boolean trace = logger.isTraceEnabled();

	@Override
	public void setUpPolicy(Sim sim){
		super.setUpPolicy(sim);
		assert sim.getParams().getMeanTimeToFail() > sim.getParams().getFinalTime() : "Random case not yet implemented";
	}	
	
	@Override
	public boolean isTargetBased() {
		return true;
	}

	@Override
	protected double doUntilNextUpdate() {
		machine.setSprint();
		//On each review period, we want to bring a change in the surplus of surplusLevelDelta
		double surplusDelta = computeSurplusDelta(currentSetup);
		return currentSetup.computeMinDeltaTimeToSurplusLevel(surplusDelta + currentSetup.getSurplus(), productionProcess, demandProcess);
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
				if(trace){logger.trace("Skipping " + item + " because it is at its target already");}
				continue;
			}
			double surplusDelta = computeSurplusDelta(item);
			double timeToReach = item.computeMinDeltaTimeToSurplusLevel(surplusDelta + currentSetup.getSurplus(), productionProcess, demandProcess);
			double averageMu = surplusDelta/timeToReach + item.getDemandRate();
			double cMu = item.getBacklogCostRate()*averageMu;
			if(trace){logger.trace("Average cmu for " + item + " is " + cMu);}
			if (cMu > maxCMu){
				maxCMu = cMu;
				nextItem = item;
			}
		}
		if(trace){logger.trace("Maximizing cmu is " + nextItem);}
		return nextItem;
	}
	
	protected double computeSurplusDelta(Item item){
		double surplusLevelDelta = policyParams.getHedgingThresholdDifference(item);
		if(trace){logger.trace("If producing " + item + " we would like a Delta suplus of " + surplusLevelDelta);}
		if (surplusLevelDelta + item.getSurplus() > item.getSurplusTarget()){
			surplusLevelDelta = item.getSurplusTarget() - item.getSurplus();
			if (trace){logger.trace("Truncating delta in surplus to " + surplusLevelDelta + " so that target is not exceeded");}
		}
		assert surplusLevelDelta >= -1.0 : "Surplus delta cannot be " + surplusLevelDelta;
		return surplusLevelDelta;
	}
}


