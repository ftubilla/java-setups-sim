package policies;

import system.Item;

public class ClearTheLongestTimePolicy extends ClearTheLargestWeightedDeviationPolicy {

    @Override
    protected Double getDeviationWeight(Item item) {
        return 1 / ( item.getProductionRate() - item.getDemandRate() );
    }

}
