package policies;

import system.Item;

public class ClearTheLongestCompensatedTimePolicy extends ClearTheLargestWeightedDeviationPolicy {

    @Override
    protected Double getDeviationWeight(Item item) {
        return 1 / ( item.getProductionRate() * this.machine.getEfficiency() - item.getDemandRate() );
    }

}
