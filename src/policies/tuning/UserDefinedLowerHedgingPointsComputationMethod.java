package policies.tuning;

import java.util.Map;

import lombok.extern.apachecommons.CommonsLog;
import sim.Sim;
import system.Item;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

@CommonsLog
public class UserDefinedLowerHedgingPointsComputationMethod implements ILowerHedgingPointsComputationMethod {

	private Map<Item, Double> lowerHedgingPoints = Maps.newHashMap();
	
	@Override
	public void compute(Sim sim) {
		Optional<ImmutableList<Double>> optional = sim.getParams().getPolicyParams().getUserDefinedLowerHedgingPoints();
		if (optional.isPresent()) {
			ImmutableList<Double> listHedgingPoints = optional.get();
			for (Item item : sim.getMachine()) {
				lowerHedgingPoints.put(item, listHedgingPoints.get(item.getId()));
				log.debug(String.format("Setting the lower hedging point of %s to %.5f (user-defined)",
						item, lowerHedgingPoints.get(item)));
			}
		} else {
			throw new Error("No lower hedging points were given!!");
		}
	}

	@Override
	public double getLowerHedgingPoint(Item item) {		
		return lowerHedgingPoints.get(item);
	}

}


