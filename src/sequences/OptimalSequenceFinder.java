package sequences;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Sets;

import lombok.extern.apachecommons.CommonsLog;
import system.Item;

@CommonsLog
public class OptimalSequenceFinder {

    public static final double COST_TOLERANCE = 1e-4;
    public static final int TIMEOUT_MIN = 45;

    private final Set<Item> items;
    private final double machineEff;
    private final OptimalFCyclicScheduleComparator scheduleComparator;

    private OptimalFCyclicSchedule optimalSchedule = null;

    public OptimalSequenceFinder(final Collection<Item> items, final double machineEff) {
        this.items = Sets.newHashSet(items);
        this.machineEff = machineEff;
        this.scheduleComparator = new OptimalFCyclicScheduleComparator(COST_TOLERANCE);
    }

    public OptimalFCyclicSchedule find(final int maxLength) throws Exception {
        return this.find(maxLength, 1);
    }
    
    /**
     * Finds the optimal f-Cyclic schedule among all schedules of length up to the given
     * max length and using the given number of threads.
     * 
     * @param maxLength
     * @param numThreads
     * @return Optimal f-Cyclic schedule
     * @throws Exception
     */
    public OptimalFCyclicSchedule find(final int maxLength, final int numThreads) throws Exception {

        if ( maxLength < items.size() ) {
            throw new IllegalArgumentException("Max. Length needs to be at least equal to " + items.size());
        }

        // Initialize the data structures to traverse the tree
        Queue<SearchNode> nodes = new LinkedList<>();
        Set<ProductionSequence> searchedSequences = new HashSet<>();
        // We can arbitrarily fix the first item to item 0
        nodes.add(new SearchNode(this.items.iterator().next()));

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        
        while ( !nodes.isEmpty() ) {
            // Take the next search node
            SearchNode nextNode = nodes.poll();
            if ( log.isTraceEnabled() ) {
                log.trace(String.format("Retrieving next search node of size %d from pool with %d nodes",
                    nextNode.getSize(), nodes.size()));
            }
            if ( nextNode.containsAllItems(this.items) && nextNode.endPointsDiffer() ) {
                log.trace("Solving optimization problem for retrieved node");
                // The sequence is valid, so a cost can be computed if not already present
                ProductionSequence nodeSequence = nextNode.getProductionSequence();
                if ( !searchedSequences.contains(nodeSequence) ) {
                    searchedSequences.add( nodeSequence );
                    searchedSequences.addAll( nodeSequence.getInversions() );
                    // Start a new task to run the optimization problem for the schedule
                    final OptimalSequenceFinder thisFinder = this;
                    Runnable task = new Runnable() {
                        @Override
                        public void run() {
                            OptimalFCyclicSchedule optimalScheduleForSequence = 
                                    new OptimalFCyclicSchedule(nodeSequence, machineEff);
                            try {
                                optimalScheduleForSequence.compute();
                            } catch (Exception e) {
                                throw new RuntimeException("Could not optimize schedule " + optimalScheduleForSequence);
                            }
                            thisFinder.submitSchedule(optimalScheduleForSequence);
                        }
                    };
                    executor.execute(task);
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
        executor.shutdown();
        executor.awaitTermination(TIMEOUT_MIN, TimeUnit.MINUTES);

        return optimalSchedule;
    }

    private synchronized void submitSchedule(final OptimalFCyclicSchedule schedule) {
        log.trace(String.format("Looking at sequence %s with cost %.5f", schedule.getSequence(), schedule.getScheduleCost()));
        boolean isBetterSchedule = false;
        if ( this.optimalSchedule == null ) {
            isBetterSchedule = true;
        } else {
            if ( this.scheduleComparator.compare(schedule, this.optimalSchedule) < 0 ) {
                isBetterSchedule = true;
            }
        }
        if ( isBetterSchedule ) {
            log.debug(String.format("Found better sequence %s with cost %.5f", schedule.getSequence(), schedule.getScheduleCost()));
            this.optimalSchedule = schedule;
        }
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
