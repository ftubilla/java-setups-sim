package policies;

import system.Item;

public class ClearTheLargestDeviationWorkPolicy extends ClearTheLargestWeightedDeviationPolicy {

    /**
     * Items are weighted by 1/productionRate.
     * 
     * @param item
     * @return
     */
    @Override
    protected Double getDeviationWeight(Item item) {
        return 1.0 / item.getProductionRate();
    }

}
