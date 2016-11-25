package sequences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import lombok.EqualsAndHashCode;
import system.Item;

/**
 * An f-cyclic production sequence
 *
 */
@EqualsAndHashCode
public class ProductionSequence implements Iterable<Item> {

    private String cachedToString;

    private final List<Item> sequence;
    private final Map<Item, List<Integer>> itemPositions;
    private final Map<Integer, List<Integer>> inBetweenPositions;

    /**
     * Creates a new production sequence based on the given array of items.
     * @param sequence
     */
    public ProductionSequence(Item... sequence) {
        this.sequence = new ArrayList<>();
        this.itemPositions = new HashMap<>();
        // Fill in the items and store the sequence positions at which each item occurs
        for ( Item item : sequence ) {
            this.sequence.add(item);
            if ( !this.itemPositions.containsKey(item) ) {
                this.itemPositions.put(item, new ArrayList<>());
            }
            this.itemPositions.get(item).add( this.sequence.size() - 1 );
        }
        // Fill in the list of positions in between successive positions of an item
        this.inBetweenPositions = new HashMap<>();
        for ( int n = 0; n < this.sequence.size(); n++ ) {
            this.inBetweenPositions.put(n, new ArrayList<>());
            Item itemInPosition = this.sequence.get(n);
            int m = n;
            while ( true ) {
                m = (m + 1) % this.sequence.size();
                if ( this.sequence.get(m).equals(itemInPosition) ) {
                    if ( this.inBetweenPositions.get(n).isEmpty() ) {
                        // Check that at least we found one item in between
                        throw new IllegalArgumentException("Sequence is not valid, since the same item appears in two contiguous positions");
                    }
                    break;
                } else {
                    this.inBetweenPositions.get(n).add(m);
                }
            }
        }
    }

    public ImmutableList<Integer> getItemPositions(final Item item) {
        return ImmutableList.copyOf(this.itemPositions.getOrDefault(item, Lists.newArrayList()));
    }

    public Item getItemAtPosition(final int position) {
        if ( position >= 0 && position < this.sequence.size() ) {
            return this.sequence.get(position);
        } else {
            return null;
        }
    }

    public ImmutableList<Integer> getPositionsInBetween(final int position) {
        return ImmutableList.copyOf(this.inBetweenPositions.getOrDefault(position, Lists.newArrayList()));
    }

    public int getNextPosition(final int position) {
        // Get the positions in between the item at the given position and the next occurrence of the item
        List<Integer> inBetweenPositions = this.inBetweenPositions.get(position);
        // Find the next position (modulo size of sequence)
        return  ( inBetweenPositions.get( inBetweenPositions.size() - 1 ) + 1 ) % this.sequence.size();
    }

    public int getSize() {
        return this.sequence.size();
    }

    public Item getFirst() {
        return this.sequence.get(0);
    }

    public Item getLast() {
        return this.sequence.get( this.sequence.size() - 1 );
    }

    /**
     * Returns <code>true</code> if the given item is contained in the sequence.
     * 
     * @param item
     * @return boolean
     */
    public boolean contains(final Item item) {
        return ( !this.getItemPositions(item).isEmpty() );
    }

    /**
     * Returns <code>true</code> if the given sequence is equivalent to the current one,
     * after accounting for a different starting point.
     * 
     * @param otherSequence
     * @return boolean
     */
    public boolean isEquivalent(final ProductionSequence otherSequence) {
        if ( otherSequence.getSize() == this.getSize() && otherSequence.contains( this.getFirst() ) ) {
            // Try starting the sequence at each possible position where the first item occurs and see if the
            // two sequences coincide
            for ( int start : otherSequence.getItemPositions(this.getFirst()) ) {
                boolean allEqual = true;
                for ( int n = 0; n < this.getSize(); n++ ) {
                    // Check that the items at each position coincide
                    int m = ( n + start ) % this.getSize();
                    if ( !this.getItemAtPosition(n).equals( otherSequence.getItemAtPosition(m) )) {
                        allEqual = false;
                        break;
                    }
                }
                if ( allEqual ) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        if ( this.cachedToString == null ) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for ( int n = 0; n < this.sequence.size(); n++ ) {
                sb.append(this.sequence.get(n).getId());
                if ( n < this.sequence.size() - 1 ) {
                    sb.append(",");
                } else {
                    sb.append("]");
                }
            }
            this.cachedToString = sb.toString();
        }
        return this.cachedToString;
    }

    @Override
    public Iterator<Item> iterator() {
        return Collections.unmodifiableList( this.sequence ).iterator();
    }
}
