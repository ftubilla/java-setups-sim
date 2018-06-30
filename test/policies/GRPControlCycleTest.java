package policies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.google.common.collect.Maps;

import policies.GRPControlCycle.GRPRunInfo;
import sequences.ProductionSequence;
import system.Item;
import system.Machine;
import util.SimBasicTest;

public class GRPControlCycleTest extends SimBasicTest {

    @Mock private Machine machine;
    @Mock private Item item0;
    @Mock private Item item1;

    @Test
    public void testNextAndHasNext() {

        // Test a sequence 0-1 with the following data
        // surplusTarget 10, 20
        // sprintingTarget 5, 15
        // gain matrix [[ 0.1, 0.2 ], [0.3, 0.4]]

        Mockito.when( machine.getNumItems() ).thenReturn( 2 );
        Mockito.when( machine.getItemById(0) ).thenReturn( item0 );
        Mockito.when( machine.getItemById(1) ).thenReturn( item1 );

        Map<Item, Double> surplusTarget = Maps.newHashMap();
        surplusTarget.put( item0, 10.0 );
        surplusTarget.put( item1, 20.0 );

        double[] sprintingTimeTarget = { 5.0, 15.0 };
        double[][] gainMatrix = { {0.1, 0.2}, {0.3, 0.4} };
        double tol = 1e-4;

        // Case I: surplus below target
        Mockito.when( item0.getSurplus() ).thenReturn( 5.0 );
        Mockito.when( item1.getSurplus() ).thenReturn( 10.0 );
        ProductionSequence sequence = new ProductionSequence(item0, item1);
        GRPControlCycle cycle1 = new GRPControlCycle( machine, sequence, surplusTarget, sprintingTimeTarget, gainMatrix );

        assertTrue( cycle1.hasNext() );
        GRPRunInfo run1 = cycle1.next();
        assertEquals( item0, run1.getItem() );
        assertEquals( 5.0 + gainMatrix[0][0] * (10 - 5) + gainMatrix[0][1] * (20 - 10), run1.getRunDuration(), tol );

        assertTrue( cycle1.hasNext() );
        GRPRunInfo run2 = cycle1.next();
        assertEquals( item1, run2.getItem() );
        assertEquals( 15.0 + gainMatrix[1][0] * (10 - 5) + gainMatrix[1][1] * (20 - 10), run2.getRunDuration(), tol );

        assertFalse( cycle1.hasNext() );

        // Case II: surplus above target
        Mockito.when( item0.getSurplus() ).thenReturn( 5000.0 );
        Mockito.when( item1.getSurplus() ).thenReturn( 10.0 );
        GRPControlCycle cycle2 = new GRPControlCycle( machine, sequence, surplusTarget, sprintingTimeTarget, gainMatrix );

        assertTrue( cycle2.hasNext() );
        run1 = cycle2.next();
        assertEquals( item0, run1.getItem() );
        // We should still get the run but with 0 duration
        assertEquals( 0, run1.getRunDuration(), tol );

        assertTrue( cycle2.hasNext() );
        run2 = cycle2.next();
        assertEquals( item1, run2.getItem() );
        assertEquals( 0, run2.getRunDuration(), tol );

        assertFalse( cycle2.hasNext() );
        
    }

}
