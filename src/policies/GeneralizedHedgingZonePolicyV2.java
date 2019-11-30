package policies;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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
 * This new version separates the policy into steps that are more consistent
 * with the paper write-up.
 * 
 * @author ftubilla
 *
 */
@CommonsLog
public abstract class GeneralizedHedgingZonePolicyV2 extends AbstractPolicy {

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
     * A lower bound estimate on the time it will take for the current item to reach its target. The more accurate
     * this estimate, the faster the simulation.
     * 
     * @param machine
     * @return time to target lower bound
     */
    protected abstract double currentSetupMinTimeToTarget(Machine machine);

    protected abstract boolean isTimeToChangeOver();

    protected abstract double getTarget(Item item);

    @Override
    protected Item nextItem() {

        if (!isTimeToChangeOver()) {
            log.trace("It is not time to change over yet, so returning null as the next item!");
            return null;
        }

        // Compute the set of items whose deviation exceeds the hedging zone
        Set<Item> hedgingZoneReadyItems = computeHedgingZoneReadySet(this.currentSetup, this.sortedItems, this::getTarget, this.hedgingZoneSize);

        // Get the subset of highest priority items
        Set<Item> highestPriorityReadyItems = computeHighestPrioritySubset(hedgingZoneReadyItems, this.priorityComparator);

        // Final pass to the ready items; check that the set is not empty
        Set<Item> readyItems = fillReadySetIfEmpty(highestPriorityReadyItems, this.currentSetup, this.sortedItems);

        // Final step: select the ready item with the highest ratio of deviation to threshold difference
        Optional<Item> readyItem = selectItemFromReadySet(readyItems, this::getTarget, this.hedgingZoneSize);

        if ( readyItem.isPresent() ) {
            return readyItem.get();
        } else {
            throw new RuntimeException("I should have been able to find an item");
        }

    }

    @Override
    protected ControlEvent onReady() {
        // Note: cruising is not available in this policy
        this.machine.setSprint();
        double lowerBound = this.currentSetupMinTimeToTarget(this.machine);
        return new ControlEvent(this.clock.getTime().add(lowerBound));
    }

    @Override
    public boolean isTargetBased() {
        // Since the target may not be constant, in general the policy is not strictly target based
        return false;
    }

    public static Set<Item> computeHedgingZoneReadySet(final Item currentSetup, final Iterable<Item> items, Function<Item, Double> getTargetImpl,
            Map<Item, Double> hedgingZone) {
        Set<Item> hedgingZoneReadyItems = StreamSupport.stream(items.spliterator(), false)
                .filter( item -> !item.equals(currentSetup) &&
                                    getTargetImpl.apply(item) - item.getSurplus() > hedgingZone.get(item) )
                .peek(item -> log.trace(String.format("%s is in the set R(1)", item)))
                .collect(Collectors.toSet());
        return hedgingZoneReadyItems;
    }

    public static Set<Item> computeHighestPrioritySubset(Set<Item> hedgingZoneReadyItems, IPriorityComparator priorityComparator) {
        Set<Item> highestPriorityItems = new HashSet<>();
        Item highestPriorityItem = null;
        for ( Item item : hedgingZoneReadyItems ) {
            if ( highestPriorityItem == null ) {
                highestPriorityItem = item;
                highestPriorityItems.add(item);
            } else {
                int compare = priorityComparator.compare(item, highestPriorityItem);
                if ( compare == 0 ) {
                    // This item has equal priority to the highest found so far. Add to the set
                    highestPriorityItems.add(item);
                } else {
                    if ( compare < 0 ) {
                        // This item has greater priority; reset the set and add it
                        highestPriorityItem = item;
                        highestPriorityItems.clear();
                        highestPriorityItems.add(item);
                    }
                }
            }
        }
        return highestPriorityItems;
    }

    public static Set<Item> fillReadySetIfEmpty(Set<Item> highestPriorityReadyItems, Item currentSetup,
            Iterable<Item> allItems) {
        if ( highestPriorityReadyItems.isEmpty() ) {
            // Add all items except for the current setup
            Set<Item> readyItems = StreamSupport.stream(allItems.spliterator(), false).collect(Collectors.toSet());
            readyItems.remove(currentSetup);
            return readyItems;
        } else {
            return highestPriorityReadyItems;
        }
    }

    public static Optional<Item> selectItemFromReadySet(final Set<Item> readyItems, Function<Item, Double> getTargetImpl, Map<Item, Double> hedgingZone) {
        // Sort by increasing index so that in case of ties the lowest-index is returned
        Optional<Pair<Item, Double>> maximizingPairOpt = 
                readyItems.stream()
                .sorted(Comparator.comparingInt(Item::getId))
                .map( item -> Pair.of(item,
                   ( getTargetImpl.apply(item) - item.getSurplus() ) / hedgingZone.get(item) ) )
                .max( Comparator.comparingDouble( ( Pair<Item, Double> pair ) -> pair.getRight() ) );
        Item returnItem = null;
        if ( maximizingPairOpt.isPresent() ) {
            Pair<Item, Double> maximizingPair = maximizingPairOpt.get();
            log.trace(String.format("Item %s had the largest ratio of %.5f", maximizingPair.getLeft(), maximizingPair.getRight()));
            returnItem = maximizingPair.getLeft();
        }
        return Optional.ofNullable(returnItem);
    }

}
