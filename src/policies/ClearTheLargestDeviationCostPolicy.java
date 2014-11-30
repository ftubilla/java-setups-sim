package policies;

import system.Item;

public class ClearTheLargestDeviationCostPolicy extends ClearTheLargestWeightedDeviationPolicy {

	/**
	 * Devaitions are weighted by the item's <emph>backlog</emph> cost rate.
	 */
	@Override
	protected Double getDeviationWeight(Item item) {
		return item.getBacklogCostRate();
	}
		
}


