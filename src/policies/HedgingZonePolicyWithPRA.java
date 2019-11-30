package policies;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import lombok.extern.apachecommons.CommonsLog;
import system.Item;

/**
 * A version of the HZP with production run anticipation (PRA), where we try to
 * "squeeze in" a production run of some item if it fits. That is, if the time to
 * complete a run of this item would not lower the surplus of any other item beyond
 * its ideal surplus deviation, then the run "fits".
 * 
 * @author ftubilla
 *
 */
@Deprecated
@CommonsLog
public class HedgingZonePolicyWithPRA extends HedgingZonePolicy {

    // TODO Add unit test

    // TODO Move this parameter to PolicyParams
    public static final double TIME_RATIO_UPPER_BOUND = 0.25;

    @Override
    protected Optional<Item> selectItemFromReadySet(final Set<Item> readyItems) {

        log.trace(String.format("The ready items are %s", readyItems));
        
        // Find the minimum time available to leave the hedging zone
        Stream<Pair<Item, Double>> itemTimeAvailStream = 
                readyItems.stream().map( item -> {
                    double lowerHedgingPoint = this.lowerHedgingPoints.getLowerHedgingPoint(item);
                    double timeToLowerHP = 0;
                    if ( item.getSurplus() > lowerHedgingPoint ) {
                        timeToLowerHP= item.getFluidTimeToSurplusLevel(this.lowerHedgingPoints.getLowerHedgingPoint(item));
                    }
                    return Pair.of(item, timeToLowerHP);
        }).peek(pair ->
        log.trace(String.format("Item %s had %.2f time units before reaching its lower hedging point %.2f",
                pair.getLeft(), pair.getRight(), this.lowerHedgingPoints.getLowerHedgingPoint(pair.getLeft()))));

        Optional<Pair<Item, Double>> constrainingItem = itemTimeAvailStream
                .min( (p1, p2) -> Double.compare(p1.getRight(), p2.getRight()));

        log.debug(String.format("Constraining item for PRA is %s", constrainingItem));
        Optional<Double> timeAvailable = constrainingItem.map(p -> p.getRight());

        // Find the item whose run can be accommodated in this time
        Optional<Item> praItem = Optional.empty();
        if ( timeAvailable.isPresent() && timeAvailable.get() > 0 ) {
            Stream<Pair<Item, Double>> timeRatios = readyItems.stream().map(item -> {
                double target = item.getSurplusTarget();
                double surplus = item.getSurplus();
                double setupTime = item.getSetupTime();
                double demandRate = item.getDemandRate();
                double productionRate = item.getProductionRate();
                double runTime = setupTime + ( target - surplus + setupTime * demandRate ) / ( productionRate - demandRate );
                double timeRatio = runTime / timeAvailable.get();
                return Pair.of(item, timeRatio);
            }).peek( p -> log.trace(String.format("Item %s has a time ratio of %.2f", 
                        p.getLeft(), p.getRight())));

            // Find a new item that has a time ratio lower than 1 + tol 
            praItem = timeRatios.filter( pair -> 
                    !pair.getLeft().equals(this.currentSetup) && pair.getRight() <= TIME_RATIO_UPPER_BOUND )
                .map( Pair::getLeft ).min(this.priorityComparator);
        }
        // If the item is not available, use the super
        if ( praItem.isPresent() ) {
            log.debug(String.format("Doing a PRA run of item %s (current setup is %s)",
                    praItem.get(), this.currentSetup));
            return praItem;
        } else {
            log.debug(String.format("No PRA run found."));
            return super.selectItemFromReadySet(readyItems);
        }

    }

}
