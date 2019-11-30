package policies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.UtilMethods.c;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

import params.Params;
import system.Item;
import util.SimBasicTest;

public class GeneralizedHedgingZonePolicyV2Test extends SimBasicTest {

    private Item item0;
    private Item item1;
    private Item item2;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        Params params = Params.builder()
                .numItems(3)
                .initialDemand(c(10, 20, 30))
                .productionRates(c(1, 1, 1))
                .build();
        this.item0 = new Item(0, params);
        this.item1 = new Item(1, params);
        this.item2 = new Item(2, params);
    }

    @Test
    public void testComputeHedgingZoneReadySet() {
        Set<Item> items = new HashSet<>();
        items.add(item0);
        items.add(item1);
        items.add(item2);
        Map<Item, Double> hedgingZone = new HashMap<>();
        hedgingZone.put(item0, 12.0);   // Item 0 is in its hedging zone
        hedgingZone.put(item1, 10.0);   // Item 1 is outside its hedging zone
        hedgingZone.put(item2, 15.0);   // Item 2 is outside its hedging zone
        // Case I current setup is item 0
        Set<Item> readyItems = GeneralizedHedgingZonePolicyV2.computeHedgingZoneReadySet(item0, items, item -> 0.0, hedgingZone);
        assertEquals( Sets.newHashSet(item1, item2), readyItems);
        // Case II current setup is item 1
        readyItems = GeneralizedHedgingZonePolicyV2.computeHedgingZoneReadySet(item1, items, item -> 0.0, hedgingZone);
        assertEquals( Sets.newHashSet(item2), readyItems);
        // Case III all items in their hedging zone
        readyItems = GeneralizedHedgingZonePolicyV2.computeHedgingZoneReadySet(item0, items, item -> -20.0, hedgingZone);
        assertTrue( readyItems.isEmpty() );
    }

    @Test
    public void testComputeHighestPrioritySubset() {
        // Case I All items equal priority
        Set<Item> readyItems = Sets.newHashSet(item0, item1, item2);
        Set<Item> result = GeneralizedHedgingZonePolicyV2.computeHighestPrioritySubset(readyItems, (i1, i2) -> 0);
        assertEquals(readyItems, result);
        // Case II Items 0 and 2 have the highest priority
        Function<Item, Integer> priorityII = item -> item.getId() == 0 || item.getId() == 2 ? 0 : 1;
        result = GeneralizedHedgingZonePolicyV2.computeHighestPrioritySubset(readyItems,
                (i1, i2) -> Integer.compare(priorityII.apply(i1), priorityII.apply(i2)));
        assertEquals(Sets.newHashSet(item0, item2), result);
        // Case III Item 1 has the highest priority
        Function<Item, Integer> priorityIII = item -> item.getId() == 0 || item.getId() == 2 ? 1 : 0;
        result = GeneralizedHedgingZonePolicyV2.computeHighestPrioritySubset(readyItems,
                (i1, i2) -> Integer.compare(priorityIII.apply(i1), priorityIII.apply(i2)));
        assertEquals(Sets.newHashSet(item1), result);
        // Case IV Item 2 has the highest priority
        Function<Item, Integer> priorityIV = item -> item.getId() == 2 ? 0 : 1;
        result = GeneralizedHedgingZonePolicyV2.computeHighestPrioritySubset(readyItems,
                (i1, i2) -> Integer.compare(priorityIV.apply(i1), priorityIV.apply(i2)));
        assertEquals(Sets.newHashSet(item2), result);
        // Case V The set is empty
        result = GeneralizedHedgingZonePolicyV2.computeHighestPrioritySubset(Sets.newHashSet(),
                (i1, i2) -> Integer.compare(priorityIV.apply(i1), priorityIV.apply(i2)));
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFillReadySetIfEmpty() {
        Set<Item> allItems = Sets.newHashSet(item0, item1, item2);
        // Case I Set is not empty
        Set<Item> readySet = Sets.newHashSet(item0, item1);
        assertEquals(readySet, GeneralizedHedgingZonePolicyV2.fillReadySetIfEmpty(readySet, item2, allItems));
        // Case II Set is empty, return all items except current setup
        assertEquals(Sets.newHashSet(item0, item2),
                GeneralizedHedgingZonePolicyV2.fillReadySetIfEmpty(Sets.newHashSet(), item1, allItems));
    }

    @Test
    public void testSelectItemFromReadySet() {
        // Case I No ties
        Set<Item> readyItems = new HashSet<>();
        readyItems.add(item0);
        readyItems.add(item1);
        Map<Item, Double> hedgingZone = new HashMap<>();
        hedgingZone.put(item0, 2.0);    // Gives a ratio of 10/2 = 5
        hedgingZone.put(item1, 2.0);    // Gives a ratio of 20/2 = 10
        Optional<Item> result = GeneralizedHedgingZonePolicyV2.selectItemFromReadySet(readyItems, item -> 0.0, hedgingZone);
        assertEquals(item1, result.get());

        // Case II Ties
        hedgingZone.put(item0, 1.0);
        result = GeneralizedHedgingZonePolicyV2.selectItemFromReadySet(readyItems, item -> 0.0, hedgingZone);
        assertEquals(item0, result.get());
    }

}
