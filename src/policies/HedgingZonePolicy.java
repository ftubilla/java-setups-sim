package policies;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.annotations.VisibleForTesting;

import discreteEvent.ControlEvent;
import discreteEvent.SurplusControlEvent;
import lombok.extern.apachecommons.CommonsLog;
import policies.tuning.ILowerHedgingPointsComputationMethod;
import policies.tuning.IPriorityComparator;
import sim.Sim;
import system.Item;
import util.AlgorithmLoader;

@CommonsLog
public class HedgingZonePolicy extends AbstractPolicy {

    public static final double EXIT_HEDGING_ZONE_TOL = 1e-9;

    protected IPriorityComparator                  priorityComparator;
    protected List<Item>                           sortedItems;
    protected ILowerHedgingPointsComputationMethod lowerHedgingPoints;
    protected double                               cruisingParameter;

    @Override
    public void setUpPolicy(final Sim sim) {

        super.setUpPolicy(sim);

        // Get the item priority static comparator)
        this.priorityComparator = AlgorithmLoader.load("policies.tuning", this.policyParams.getPriorityComparator(),
                IPriorityComparator.class);

        // Sort items by priority
        this.sortedItems = StreamSupport.stream(this.machine.spliterator(), false)
                .sorted(this.priorityComparator)
                .collect(Collectors.toList());

        // Get the lower hedging points
        this.lowerHedgingPoints = AlgorithmLoader.load("policies.tuning",
                this.policyParams.getLowerHedgingPointsComputationMethod(), ILowerHedgingPointsComputationMethod.class);
        this.lowerHedgingPoints.compute(sim);

        // TODO For now, we assume the policy never cruises
        this.cruisingParameter = 0.0;
        if (this.policyParams.getUserDefinedIsCruising().isPresent() && this.policyParams.getUserDefinedIsCruising().get()) {
            log.warn("Cruising is set to true by the user but right now the policy is non cruising!!!");
            throw new RuntimeException("Cruising is not implemented yet");
        }
    }

    @Override
    protected boolean isTimeToChangeOver() {
        if ( isInTheFractionalHedgingZone( this.cruisingParameter ) ) {
            // Changeovers never occur in the fractional hedging zone
            return false;
        } else {
            return this.machine.getSetup().onOrAboveTarget();
        }
    }

    @Override
    protected ControlEvent onReady() {
        if ( isInTheFractionalHedgingZone( this.cruisingParameter ) && this.machine.getSetup().onTarget() ) {
            // Only cruise when we are in the fractional hedging zone (with fraction equal to the cruising parameter)
            // and we are at the target ZU. Set the next control event for the next time we exit the hedging zone, with
            // some small tolerance to avoid getting stuck at the boundary if the delta time to exit is exactly equal to 0
            this.machine.setCruise();
            double deltaToExit = Math.max( computeTimeToExitFractionalHedgingZone(cruisingParameter), EXIT_HEDGING_ZONE_TOL );
            return new ControlEvent( this.clock.getTime() + deltaToExit );
        } else {
            this.machine.setSprint();
            return new SurplusControlEvent(this.currentSetup, this.currentSetup.getSurplusTarget(), this.clock.getTime(),
                    this.hasDiscreteMaterial);
        }
    }

    @Override
    protected Item nextItem() {

        if (!isTimeToChangeOver()) {
            log.trace("It is not time to change over yet, so returning null as the next item!");
            return null;
        }

        // Compute the set R(f) of items whose deviation exceeds their fractional hedging zone
        Set<Item> fractionalReadyItems = this.sortedItems.stream()
                .filter( item -> !this.isInTheFractionalHedgingZone(this.cruisingParameter, item ) )
                .collect(Collectors.toSet());

        if ( fractionalReadyItems.isEmpty() ) {
            throw new RuntimeException("The policy says is time to change over but the fractional ready set is empty");
        }

        // Compute the set R(1) of items whose deviation exceeds the hedging zone
        Set<Item> hedgingZoneReadyItems = this.sortedItems.stream()
                .filter( item -> !this.isInTheFractionalHedgingZone(1.0, item) )
                .peek(item -> log.trace(String.format("%s is in the set R(1)", item)))
                .collect(Collectors.toSet());

        // Determine the ready set by taking the highest priority items in R(1) or, if it's empty, all the items in R(f)
        Set<Item> readyItems = new HashSet<Item>();
        if ( !hedgingZoneReadyItems.isEmpty() ) {
            // Case 1: set R(1) is non-empty. Get the highest priority items
            Item highestPriorityZoneReadyItem = null;
            for ( Item item : this.sortedItems ) {
                if ( hedgingZoneReadyItems.contains( item ) ) {
                    if ( highestPriorityZoneReadyItem == null ) {
                        // This is the first highest-priority item in the zone ready set. Add it to the ready set
                        highestPriorityZoneReadyItem = item;
                        readyItems.add(item);
                        log.trace(String.format("Adding %s as a high-priority ready item", item));
                    } else {
                        // Only add this item to the set if it has the same priority as the highest-priority zone ready item
                        if ( this.priorityComparator.compare(highestPriorityZoneReadyItem, item) == 0 ) {
                            readyItems.add(item);
                            log.trace(String.format("Adding %s to the high-priority ready items", item));
                        }
                    }
                }
            }
        } else {
            // Case 2: set R(1) is empty. Set the ready set to R(f)
            log.debug("The ready set R(1) is empty. Adding all items from R(f) to the ready set");
            readyItems.addAll( fractionalReadyItems );
        }

        // Final step: select the ready item with the highest ratio of deviation to threshold difference
        Optional<Item> readyItem = selectItemFromReadySet( readyItems );

        if ( readyItem.isPresent() ) {
            return readyItem.get();
        } else {
            throw new RuntimeException("I should have been able to find an item");
        }

    }

    /**
     * Returns <code>true</code> if the surplus deviation for all items is less than the difference
     * between surplus target and lower hedging point times the given cruising fraction. If the factor
     * is 0, then the system will always be outside the hedging zone.
     * 
     * @param fraction 
     * @return boolean
     */
    @VisibleForTesting
    protected boolean isInTheFractionalHedgingZone( final double fraction ) {
        for ( Item item : sortedItems ) {
            if ( !isInTheFractionalHedgingZone(fraction, item ) ) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns <code>true</code> if the surplus deviation for the given item is no greater than the
     * difference between surplus target and lower hedging point times the given fraction (cruising factor).
     * 
     * @param fraction
     * @param item
     * @return boolean
     */
    @VisibleForTesting
    protected boolean isInTheFractionalHedgingZone( final double fraction, final Item item ) {
        return item.getSurplusDeviation() <=
                    fraction  * ( item.getSurplusTarget() - lowerHedgingPoints.getLowerHedgingPoint(item) );
    }

    @VisibleForTesting
    protected double computeTimeToExitFractionalHedgingZone(final double fraction) {
        double minExitTime = Double.MAX_VALUE;
        for (Item item : this.sortedItems) {
            double zu = item.getSurplusTarget();
            double zl = this.lowerHedgingPoints.getLowerHedgingPoint(item);
            double surplusBoundary = zu - fraction * ( zu - zl );
            double exitTime = item.getFluidTimeToSurplusLevel( surplusBoundary );
            assert exitTime >= 0 : "The system is not in the hedging zone!";
            if (exitTime < minExitTime) {
                minExitTime = exitTime;
            }
        }
        return minExitTime;
    }

    protected Optional<Item> selectItemFromReadySet(final Set<Item> readyItems) {
        Optional<Pair<Item, Double>> maximizingPairOpt = 
                readyItems.stream()
                .map( item -> Pair.of(item,
                   item.getSurplusDeviation() / ( item.getSurplusTarget() - this.lowerHedgingPoints.getLowerHedgingPoint(item) ) ) )
                .max( Comparator.comparingDouble( ( Pair<Item, Double> pair ) -> pair.getRight() ) );
        Item returnItem = null;
        if ( maximizingPairOpt.isPresent() ) {
            Pair<Item, Double> maximizingPair = maximizingPairOpt.get();
            log.trace(String.format("Item %s had the largest ratio of %.5f", maximizingPair.getLeft(), maximizingPair.getRight()));
            returnItem = maximizingPair.getLeft();
        }
        return Optional.ofNullable(returnItem);
    }

    @Override
    public boolean isTargetBased() {
        return true;
    }

}
