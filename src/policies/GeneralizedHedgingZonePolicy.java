package policies;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Maps;

import discreteEvent.ControlEvent;
import lombok.extern.apachecommons.CommonsLog;
import params.PolicyParams;
import policies.tuning.ILowerHedgingPointsComputationMethod;
import policies.tuning.IPriorityComparator;
import sim.Sim;
import system.Item;
import system.Machine;
import util.AlgorithmLoader;

/**
 * Abstract class for implementing a <i>non-cruising</i> hedging zone policy,
 * with the hedging bound determined by an arbitrary hyper-surface on surplus
 * space. Concrete classes implementing this class can thus use different
 * hyper-surfaces.
 * 
 * @author ftubilla
 *
 */
@Deprecated
@CommonsLog
public abstract class GeneralizedHedgingZonePolicy extends AbstractPolicy {

    protected IPriorityComparator priorityComparator;
    protected List<Item>          sortedItems;
    protected Map<Item, Double>   hedgingZoneSize;  // The difference between the upper and lower hedging points

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
        ILowerHedgingPointsComputationMethod lowerHedgingPointsComputation = getLowerHedgingPointComputationMethod(this.policyParams);
        lowerHedgingPointsComputation.compute(sim);
        this.hedgingZoneSize = Maps.newHashMap();
        for ( Item item : this.machine ) {
            double dZItem = item.getSurplusTarget() - lowerHedgingPointsComputation.getLowerHedgingPoint(item);
            this.hedgingZoneSize.put(item, dZItem);
            log.debug(String.format("Setting the hedging zone for %s to %.3f", item, dZItem));
        }

        // Check that the policy is not meant to be used for cruising systems
        if (this.policyParams.getUserDefinedIsCruising().isPresent() && this.policyParams.getUserDefinedIsCruising().get() ) {
            log.warn("Cruising is set to true by the user but right now the policy is non cruising!!!");
            throw new RuntimeException("Cruising is not implemented yet");
        }
        if ( sim.getSurplusCostLowerBound().getIsCruising() ) {
            log.warn("The sim instance is a cruising instance (according to the lower bound), but cruising is not enabled in this policy!");
        }

    }

    /**
     * Provides an entry point for using a different hedging point computation method.
     * @param policyParams
     * @return
     */
    protected ILowerHedgingPointsComputationMethod getLowerHedgingPointComputationMethod(PolicyParams policyParams) {
        return AlgorithmLoader.load("policies.tuning",
                this.policyParams.getLowerHedgingPointsComputationMethod(), ILowerHedgingPointsComputationMethod.class);
    }

    /**
     * Return <code>true</code> if the current setup is at or above its current target.
     * 
     * @param machine
     * @return boolean
     */
    protected abstract boolean currentSetupOnOrAboveTarget(Machine machine);

    /**
     * Return <code>true</code> if the current item is inside its hedging zone.
     * 
     * @param machine
     * @param item
     * @param hedgingZoneSize The difference between upper and lower hedging points
     * @return boolean
     */
    protected abstract boolean isInTheHedgingZone(Machine machine, Item item, double hedgingZoneSize);

    /**
     * A lower bound estimate on the time it will take for the current item to reach its target. The more accurate
     * this estimate, the faster the simulation.
     * 
     * @param machine
     * @return time to target lower bound
     */
    protected abstract double currentSetupMinTimeToTarget(Machine machine);

    /**
     * Returns the current surplus deviation of the given item, based on the current target.
     * 
     * @param machine
     * @param item
     * @return surplus deviation
     */
    protected abstract double getSurplusDeviation(Machine machine, Item item);

    @Override
    protected boolean isTimeToChangeOver() {
        return this.currentSetupOnOrAboveTarget( this.machine );
    }

    @Override
    protected ControlEvent onReady() {
        // Note: cruising is not available in this policy
        this.machine.setSprint();
        double lowerBound = this.currentSetupMinTimeToTarget(this.machine);
        return new ControlEvent(this.clock.getTime().add(lowerBound));
    }

    @Override
    protected Item nextItem() {

        if (!isTimeToChangeOver()) {
            log.trace("It is not time to change over yet, so returning null as the next item!");
            return null;
        }

        // Compute the set of items R(1) whose deviation exceeds the hedging zone
        Set<Item> hedgingZoneReadyItems = this.sortedItems.stream()
                .filter( item -> !this.isInTheHedgingZone(this.machine, item, this.hedgingZoneSize.get(item)) )
                .peek(item -> log.trace(String.format("%s is in the set R(1)", item)))
                .collect(Collectors.toSet());

        // Determine the ready set by taking the highest priority items outside their hedging zone
        // If all items are in their hedging zone, then consider them all
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
            // Case 2: set R(1) is empty. Set to all items (except for the current setup)
            log.debug("The ready set R(1) is empty. Adding all items to the ready set");
            readyItems.addAll( this.sortedItems );
            readyItems.remove( this.currentSetup );
        }

        // Final step: select the ready item with the highest ratio of deviation to threshold difference
        Optional<Item> readyItem = selectItemFromReadySet( readyItems );

        if ( readyItem.isPresent() ) {
            return readyItem.get();
        } else {
            throw new RuntimeException("I should have been able to find an item");
        }

    }

    protected Optional<Item> selectItemFromReadySet(final Set<Item> readyItems) {
        // Sort by increasing index so that in case of ties the lowest-index is returned
        Optional<Pair<Item, Double>> maximizingPairOpt = 
                readyItems.stream()
                .sorted(Comparator.comparingInt(Item::getId))
                .map( item -> Pair.of(item,
                   this.getSurplusDeviation(this.machine, item) / this.hedgingZoneSize.get(item) ) )
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
        // Since the target may not be constant, in general the policy is not strictly target based
        return false;
    }

}
