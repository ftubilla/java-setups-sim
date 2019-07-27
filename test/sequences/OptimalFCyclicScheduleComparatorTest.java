package sequences;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.UtilMethods.c;

import org.junit.Test;

import params.Params;
import params.Params.ParamsBuilder;
import system.Item;
import util.SimBasicTest;

public class OptimalFCyclicScheduleComparatorTest extends SimBasicTest {

    private static final double TOL = 1e-4;

    @Test
    public void testEqualSequence() throws Exception {

        ParamsBuilder paramsBuilder = Params.builderWithDefaults();
        paramsBuilder
            .numItems(3)
            .backlogCosts(c(1.0, 1.0, 1.0))
            .inventoryHoldingCosts(c(20.0, 20.0, 20.0))
            .productionRates(c(1.0, 1.0, 1.0))
            .demandRates(c(0.3, 0.3, 0.3))
            .setupTimes(c(1,1,1));

        Params params = paramsBuilder.build();

        Item item0 = new Item(0, params);
        Item item1 = new Item(1, params);
        Item item2 = new Item(2, params);
        ProductionSequence sequence1 = new ProductionSequence(item0, item1, item2);
        ProductionSequence sequence2 = new ProductionSequence(item0, item1, item2);

        OptimalFCyclicSchedule schedule1 = new OptimalFCyclicSchedule(sequence1, 1.0);
        schedule1.compute();
        OptimalFCyclicSchedule schedule2 = new OptimalFCyclicSchedule(sequence2, 1.0);
        schedule2.compute();

        OptimalFCyclicScheduleComparator comp = new OptimalFCyclicScheduleComparator(TOL);
        assertEquals( 0, comp.compare(schedule1, schedule2) );
    }

    @Test
    public void testShorterSequenceTieBreaker() throws Exception {
        ParamsBuilder paramsBuilder = Params.builderWithDefaults();
        paramsBuilder
            .numItems(3)
            .backlogCosts(c(1.0, 1.0, 1.0))
            .inventoryHoldingCosts(c(20.0, 20.0, 20.0))
            .productionRates(c(1.0, 1.0, 1.0))
            .demandRates(c(0.3, 0.3, 0.3))
            .setupTimes(c(1,1,1));

        Params params = paramsBuilder.build();

        Item item0 = new Item(0, params);
        Item item1 = new Item(1, params);
        Item item2 = new Item(2, params);
        ProductionSequence sequence1 = new ProductionSequence(item0, item1, item2);
        ProductionSequence sequence2 = new ProductionSequence(item0, item1, item2, item0, item1, item2);

        OptimalFCyclicSchedule schedule1 = new OptimalFCyclicSchedule(sequence1, 1.0);
        schedule1.compute();
        OptimalFCyclicSchedule schedule2 = new OptimalFCyclicSchedule(sequence2, 1.0);
        schedule2.compute();

        OptimalFCyclicScheduleComparator comp = new OptimalFCyclicScheduleComparator(TOL);
        assertEquals( schedule1.getScheduleCost(), schedule2.getScheduleCost(), TOL);
        assertTrue( comp.compare(schedule1, schedule2) < 0 );
    }

    @Test
    public void testCheaperSequenceSort() throws Exception {
        ParamsBuilder paramsBuilder = Params.builderWithDefaults();
        paramsBuilder
            .numItems(3)
            .backlogCosts(c(1.0, 1.0, 1.0))
            .inventoryHoldingCosts(c(20.0, 20.0, 0.2))
            .productionRates(c(1.0, 1.0, 1.0))
            .demandRates(c(0.3, 0.3, 0.3))
            .setupTimes(c(1,1,1));

        Params params = paramsBuilder.build();

        Item item0 = new Item(0, params);
        Item item1 = new Item(1, params);
        Item item2 = new Item(2, params);
        ProductionSequence sequence1 = new ProductionSequence(item0, item1, item2);
        ProductionSequence sequence2 = new ProductionSequence(item2, item1, item2, item0, item1);

        OptimalFCyclicSchedule schedule1 = new OptimalFCyclicSchedule(sequence1, 1.0);
        schedule1.compute();
        OptimalFCyclicSchedule schedule2 = new OptimalFCyclicSchedule(sequence2, 1.0);
        schedule2.compute();

        OptimalFCyclicScheduleComparator comp = new OptimalFCyclicScheduleComparator(TOL);
        assertTrue( schedule1.getScheduleCost() < schedule2.getScheduleCost() );
        assertTrue( comp.compare(schedule1, schedule2) < 0 );
    }

    @Test
    public void testLexicographicTieBreaker() throws Exception {

        ParamsBuilder paramsBuilder = Params.builderWithDefaults();
        paramsBuilder
            .numItems(3)
            .backlogCosts(c(1.0, 1.0, 1.0))
            .inventoryHoldingCosts(c(20.0, 20.0, 20.0))
            .productionRates(c(1.0, 1.0, 1.0))
            .demandRates(c(0.3, 0.3, 0.3))
            .setupTimes(c(1,1,1));

        Params params = paramsBuilder.build();

        Item item0 = new Item(0, params);
        Item item1 = new Item(1, params);
        Item item2 = new Item(2, params);
        ProductionSequence sequence1 = new ProductionSequence(item0, item1, item2);
        ProductionSequence sequence2 = new ProductionSequence(item1, item2, item0);
        ProductionSequence sequence3 = new ProductionSequence(item2, item0, item1);

        OptimalFCyclicSchedule schedule1 = new OptimalFCyclicSchedule(sequence1, 1.0);
        schedule1.compute();
        OptimalFCyclicSchedule schedule2 = new OptimalFCyclicSchedule(sequence2, 1.0);
        schedule2.compute();
        OptimalFCyclicSchedule schedule3 = new OptimalFCyclicSchedule(sequence3, 1.0);
        schedule3.compute();

        assertEquals( schedule1.getScheduleCost(), schedule2.getScheduleCost(), TOL);
        assertEquals( schedule1.getScheduleCost(), schedule3.getScheduleCost(), TOL);

        OptimalFCyclicScheduleComparator comp = new OptimalFCyclicScheduleComparator(TOL);
        assertTrue( comp.compare(schedule1, schedule2) < 0 );
        assertTrue( comp.compare(schedule2, schedule3) < 0 );
        assertTrue( comp.compare(schedule1, schedule3) < 0 );
        assertTrue( comp.compare(schedule2, schedule1) > 0 );
        assertTrue( comp.compare(schedule3, schedule2) > 0 );
    }

}
