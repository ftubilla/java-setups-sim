package sequences;

import java.util.Iterator;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableList;

import system.Item;
import util.SimBasicTest;

public class ProductionSequenceTest extends SimBasicTest {

    @Mock Item item1;
    @Mock Item item2;
    @Mock Item item3;
    @Mock Item item4;

    @Test
    public void test() {

        Mockito.when( item1.getId() ).thenReturn(1);
        Mockito.when( item2.getId() ).thenReturn(2);
        Mockito.when( item3.getId() ).thenReturn(3);
        Mockito.when( item4.getId() ).thenReturn(4);

        // Start with a valid sequence
        ProductionSequence sequence = new ProductionSequence(item1, item2, item3, item1, item2);
        assertEquals( 5, sequence.getSize() );
        assertEquals( ImmutableList.of(0, 3), sequence.getItemPositions(item1) );
        assertEquals( ImmutableList.of(1, 4), sequence.getItemPositions(item2) );
        assertEquals( ImmutableList.of(2), sequence.getItemPositions(item3) );
        assertEquals( 3, sequence.getNextPosition(0) );
        assertEquals( 4, sequence.getNextPosition(1) );
        assertEquals( 2, sequence.getNextPosition(2) );
        assertEquals( 0, sequence.getNextPosition(3) );
        assertEquals( 1, sequence.getNextPosition(4) );
        assertEquals( ImmutableList.of(1,2), sequence.getPositionsInBetween(0) );
        assertEquals( ImmutableList.of(2,3), sequence.getPositionsInBetween(1) );
        assertEquals( ImmutableList.of(3,4,0,1), sequence.getPositionsInBetween(2) );
        assertEquals( ImmutableList.of(4), sequence.getPositionsInBetween(3) );
        assertEquals( ImmutableList.of(0), sequence.getPositionsInBetween(4) );

        assertTrue( sequence.toString().equals("[1,2,3,1,2]"));
        assertEquals( item1, sequence.getFirst() );
        assertEquals( item2, sequence.getLast() );
        assertTrue( sequence.contains(item1) );
        assertTrue( sequence.contains(item2) );
        assertTrue( sequence.contains(item3) );
        assertFalse( sequence.contains(item4) );

        ProductionSequence s1 = new ProductionSequence(item3, item1, item2, item1, item2);
        assertTrue( sequence.isEquivalent(s1) );
        
        ProductionSequence s2 = new ProductionSequence(item1, item3, item2, item1, item2);
        assertFalse( sequence.isEquivalent(s2) );

        // Try to remove an item from the iterator
        boolean exceptionThrownOnRemove = false;
        Iterator<Item> it = sequence.iterator();
        assertEquals( item1, it.next() );
        try {
            it.remove();
        } catch ( UnsupportedOperationException e ) {
            exceptionThrownOnRemove = true;
        }
        assertTrue( exceptionThrownOnRemove );
        
        
        // Now try an invalid sequence
        boolean exceptionThrown = false;
        try {
            new ProductionSequence(item1, item2, item2);
        } catch ( IllegalArgumentException e ) {
            exceptionThrown = true;
        }
        assertTrue( exceptionThrown );

    }

}
