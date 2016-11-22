package policies.tuning;

import static util.UtilMethods.c;

import java.util.Comparator;

import org.junit.Test;

import params.Params;
import params.Params.ParamsBuilder;
import system.Item;
import util.SimBasicTest;

public class CmuComparatorTest extends SimBasicTest {

    private final double tol = 1e-6;

    @Test
    public void test() {

        ParamsBuilder builder = Params.builderWithDefaults();

        // cmu_0 = 5 / 2 = 2.5
        // cmu_1 = 40 / 21 = 1.904
        // cmu_2 = 60 / 32 = 1.875
        // ordering 0 -> 1 -> 2
        builder
            .backlogCosts(c(1.0, 1.0, 2.0))
            .productionRates(c(5, 2, 1))
            .demandRates(c(10, 20, 30))
            .inventoryHoldingCosts(c(1.0, 20.0, 30.0))
            .initialDemand(c(0,0,0))
            .surplusTargets(c(0,0,0))
            .setupTimes(c(5,5,2));

        Params params = builder.build();
        Item item0 = new Item(0, params);
        Item item1 = new Item(1, params);
        Item item2 = new Item(2, params);
        
        Comparator<Item> comparator = new CMuComparator(tol);
        
        assertTrue( comparator.compare(item0, item1) < 0 );
        assertTrue( comparator.compare(item0, item2) < 0 );
        assertTrue( comparator.compare(item1, item2) < 0 );
        
        // cmu_0 = 5 / 2
        // cmu_1 = 5 / 2 + 0.99 * tol
        // cmu_2 = 5 / 2 - 0.99 * tol
        // all products have equal priority
        builder
            .backlogCosts(c(1.0, 1.0, 1.0))
            .productionRates(c(5, 5 + 2 * 0.99 * tol, 5 - 2 * 0.99 * tol))
            .inventoryHoldingCosts(c(1.0, 1.0, 1.0));

        params = builder.build();
        item0 = new Item(0, params);
        item1 = new Item(1, params);
        item2 = new Item(2, params);

        comparator = new CMuComparator(tol);

        assertTrue( comparator.compare(item0, item1) == 0 );
        assertTrue( comparator.compare(item0, item2) == 0 );
        assertTrue( comparator.compare(item1, item2) == 0 );

        // h_i = infinity for i = 0, 1, 2, and
        // cmu_0 = 5 / 2
        // cmu_1 = 2
        // cmu_2 = 6
        // ordering 2 -> 0 -> 1
        builder
            .backlogCosts(c(1.0, 1.0, 1.0))
            .productionRates(c(2.5, 2, 5))
            .inventoryHoldingCosts(c(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));

        params = builder.build();
        item0 = new Item(0, params);
        item1 = new Item(1, params);
        item2 = new Item(2, params);

        comparator = new CMuComparator(tol);

        assertTrue( comparator.compare(item0, item1) < 0 );
        assertTrue( comparator.compare(item0, item2) > 0 );
        assertTrue( comparator.compare(item1, item2) > 0 );

    }

    @Test
    public void testCCostCalculator() {
        CMuComparator comparator = new CMuComparator(tol);
        assertEquals( 5.0, comparator.computeCCost(5.0, Double.POSITIVE_INFINITY), tol);
        assertEquals( 4.0, comparator.computeCCost(Double.POSITIVE_INFINITY, 4.0), tol);
        assertEquals( Double.POSITIVE_INFINITY, comparator.computeCCost(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), tol);
        assertEquals( 20 / 9.0, comparator.computeCCost(5.0, 4.0), tol);
    }

}
