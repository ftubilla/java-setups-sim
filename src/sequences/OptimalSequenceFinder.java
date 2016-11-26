package sequences;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import com.google.common.collect.Sets;

import lombok.extern.apachecommons.CommonsLog;
import system.Item;

@CommonsLog
public class OptimalSequenceFinder {

    private final Set<Item> items;
    private final double machineEff;

    public OptimalSequenceFinder(final Collection<Item> items, final double machineEff) {
        this.items = Sets.newHashSet(items);
        this.machineEff = machineEff;
    }

    public OptimalFCyclicSchedule find(final int maxLength) throws Exception {

        if ( maxLength < items.size() ) {
            throw new IllegalArgumentException("Max. Length needs to be at least equal to " + items.size());
        }

        // Initialize the data structures
        Queue<SearchNode> nodes = new LinkedList<>();
        Set<ProductionSequence> searchedSequences = new HashSet<>();
        Double lowestCost = Double.MAX_VALUE;
        OptimalFCyclicSchedule bestSchedule = null;
        nodes.add(new SearchNode(new Item[0]));

        while ( !nodes.isEmpty() ) {
            // Take the next search node
            SearchNode nextNode = nodes.poll();
            log.trace(String.format("Retrieving next search node of size %d", nextNode.getSize()));
            if ( nextNode.containsAllItems(this.items) && nextNode.endPointsDiffer() ) {
                // The sequence is valid. Try computing its cost
                ProductionSequence nodeSequence = nextNode.getProductionSequence();
                if ( !searchedSequences.contains(nodeSequence) ) {
                    OptimalFCyclicSchedule optimalSchedule = new OptimalFCyclicSchedule(nodeSequence, machineEff);
                    optimalSchedule.compute();
                    double cost = optimalSchedule.getScheduleCost();
                    log.trace(String.format("Search node sequence %s has cost %.5f", nodeSequence, cost));
                    if ( cost < lowestCost ) {
                        log.debug(String.format("Found better sequence %s with cost %.5f", nodeSequence, cost));
                        lowestCost = cost;
                        bestSchedule = optimalSchedule;
                    }
                    searchedSequences.add( nodeSequence );
                    searchedSequences.addAll( nodeSequence.getInversions() );
                }
            }
            // Branch if possible
            if ( nextNode.getSize() < maxLength ) {
                for ( Item item : this.items ) {
                    if ( nextNode.canAppend(item) ) {
                        log.debug(String.format("Branching search node by adding item %s", item.getId()));
                        SearchNode newNode = nextNode.append(item);
                        nodes.add(newNode);
                    }
                }
            }
        }
        return bestSchedule;
    }

    /**
     * A helper class representing a search node with a (possibly invalid) sequence segment
     *
     */
    public static class SearchNode {

        private final Item[] sequenceArray;
        private final Set<Item> containedItems;

        public SearchNode(final Item... sequence) {
            this.sequenceArray = sequence;
            this.containedItems = Sets.newHashSet(sequence);
        }

        public boolean canAppend(final Item item) {
            if ( this.sequenceArray.length == 0 ) {
                return true;
            } else {
                return !this.sequenceArray[this.sequenceArray.length-1].equals(item);
            }
        }

        public boolean endPointsDiffer() {
            // If the first and last items are equal, the sequence is not a valid sequence yet
            if ( this.sequenceArray.length > 1 ) {
                return !this.sequenceArray[0].equals(this.sequenceArray[this.sequenceArray.length - 1]);
            } else {
                return false;
            }
        }
        
        public SearchNode append(final Item item) {
            if ( !this.canAppend(item) ) {
                throw new IllegalArgumentException("Cannot append an item in two consecutive positions!");
            }
            Item[] newSequence = new Item[this.sequenceArray.length + 1];
            for ( int n = 0; n < this.sequenceArray.length; n++ ) {
                newSequence[n] = this.sequenceArray[n];
            }
            newSequence[ this.sequenceArray.length ] = item;
            return new SearchNode(newSequence);
        }

        public int getSize() {
            return this.sequenceArray.length;
        }

        public ProductionSequence getProductionSequence() {
            return new ProductionSequence(this.sequenceArray);
        }

        public boolean containsAllItems(final Set<Item> items) {
            return this.containedItems.containsAll(items);
        }

    }

}
