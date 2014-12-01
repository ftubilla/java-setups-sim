package policies.tuning;

import java.util.Map;


import lombok.extern.apachecommons.CommonsLog;
import sim.Sim;
import system.Item;

import com.google.common.collect.Maps;

/**
 * Computes the lower hedging point based on the make-to-order cost lower bound (see page 147 of Tubilla 2011).
 * 
 * @author ftubilla
 *
 */
@CommonsLog
public class MakeToOrderBoundBasedLowerHedgingPointsComputationMethod implements ILowerHedgingPointsComputationMethod {

	private Map<Item, Double> lowerHedgingPoints = Maps.newHashMap();
	
	@Override
	public void compute(Sim sim) {
		if (sim.getParams().getPolicyParams().getUserDefinedLowerHedgingPoints().isPresent()) {
			log.warn("Warning. Lower hedging points were given in the params, but they will be overriden.");
		}
		for (Item item : sim.getMachine()) {
			double y = sim.getMakeToOrderLowerBound().getIdealSurplusDeviation(item.getId());
			double S = item.getSetupTime();
			double d = item.getDemandRate();
			double dZ = y - S*d;
			double lowerHP = item.getSurplusTarget() - dZ;
			log.debug(String.format("Setting the lower hedging point for %s to %.3f", item, lowerHP));
			lowerHedgingPoints.put(item, lowerHP);
		}
	}

	@Override
	public double getLowerHedgingPoint(Item item) {
		return lowerHedgingPoints.get(item);
	}

}


