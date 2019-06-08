package policies;

import lombok.extern.apachecommons.CommonsLog;
import sim.Sim;
import system.Item;

@CommonsLog
public abstract class ClearTheLargestWeightedDeviationPolicy extends ClearingPolicy {

    /**
     * Returns the weight of the deviation for choosing the item with the
     * largest weighted deviation. Returns <code>null</code> if there is no
     * weight for the given item.
     * 
     * @param item
     * @return Double
     */
    protected abstract Double getDeviationWeight(Item item);

    /**
     * Returns the item with largest weighted deviation (breaking ties by item
     * id and excluding in such a case the current setup).
     */
    @Override
    protected Item nextItem() {

        if (!isTimeToChangeOver()) {
            return null;
        }

        double largestWeightedDeviation = 0.0;
        Item nextItem = null;

        for (Item item : machine) {
            Double weight = getDeviationWeight(item);
            if (weight == null) {
                // This will throw an NPE
                log.fatal("No weight given for " + item + ". Check implementation of your policy");
            }
            double weightedDeviation = item.getSurplusDeviation() * weight;
            if (weightedDeviation > largestWeightedDeviation * ( 1 + Sim.SURPLUS_RELATIVE_TOLERANCE ) ) {
                largestWeightedDeviation = weightedDeviation;
                nextItem = item;
            }
            log.trace(item + " has a weighted surplus deviation of " + weightedDeviation
                    + " and the max weighted dev so far is " + largestWeightedDeviation);
        }

        if (nextItem != null) {
            // Most likely scenario
            log.trace("Next item to produce is " + nextItem + " which has the largest weighted deviation");
        } else {
            // If all items have the same weighted deviation, find the next item
            // that's not the current setup
            for (Item item : machine) {
                if (item != machine.getSetup()) {
                    nextItem = item;
                    log.trace("All items had the same surplus deviation. Changing over to the next item " + nextItem);
                    break;
                }
            }
        }
        return nextItem;
    }

}
