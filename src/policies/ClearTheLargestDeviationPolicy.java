package policies;

import system.Item;

public class ClearTheLargestDeviationPolicy extends ClearTheLargestWeightedDeviationPolicy {

	/**
	 * The deviation of the items is not weighted in this policy, so it returns 1.0.
	 * @param item
	 * @return 1.0.
	 */
	@Override
	protected Double getDeviationWeight(Item item) {
		return 1.0;
	}


}


