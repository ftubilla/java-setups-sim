package policies.tuning;

import static util.UtilMethods.c;

import java.util.Comparator;

import org.junit.Test;

import params.Params;
import params.Params.ParamsBuilder;
import system.Item;
import util.SimBasicTest;

public class CmuComparatorTest extends SimBasicTest {

    @Test
    public void test() {
        ParamsBuilder builder = Params.builderWithDefaults();
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
        
        Comparator<Item> comparator = new CMuComparator();
        
        assertTrue( comparator.compare(item0, item1) < 0 );
        assertTrue( comparator.compare(item0, item2) < 0 );
        assertTrue( comparator.compare(item1, item2) == 0);
    }

}
