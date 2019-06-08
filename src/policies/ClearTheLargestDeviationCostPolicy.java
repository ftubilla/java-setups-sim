package policies;

import system.Item;

public class ClearTheLargestDeviationCostPolicy extends ClearTheLargestWeightedDeviationPolicy {

    @Override
    protected Double getDeviationWeight(Item item) {
        return item.getCCostRate();
    }

}
