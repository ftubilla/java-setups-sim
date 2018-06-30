package sequences;

import static org.junit.Assert.assertEquals;
import static util.UtilMethods.c;

import org.junit.Test;

import params.Params;
import params.Params.ParamsBuilder;
import system.Item;
import util.SimBasicTest;

public class OptimalFCCyclicScheduleTest extends SimBasicTest {

    @Test
    public void testSymmetricSystem() throws Exception {

        ParamsBuilder paramsBuilder = Params.builderWithDefaults();
        paramsBuilder
            .numItems(3)
            .surplusTargets(c(0.0, 0.0, 0.0))
            .initialDemand(c(0.0, 20.0, 30.0))
            .backlogCosts(c(1.0, 1.0, 1.0))
            .inventoryHoldingCosts(c(20.0, 20.0, 20.0))
            .productionRates(c(1.0, 1.0, 1.0))
            .demandRates(c(0.3, 0.3, 0.3))
            .setupTimes(c(1,1,1));

        Params params = paramsBuilder.build();

        Item item0 = new Item(0, params);
        Item item1 = new Item(1, params);
        Item item2 = new Item(2, params);
        ProductionSequence sequence = new ProductionSequence(item0, item1, item2);

        OptimalFCyclicSchedule schedule = new OptimalFCyclicSchedule(sequence, 1.0);
        schedule.compute();
        
        double tol = 1e-3;
        // Check symmetry
        assertEquals( schedule.getOptimalSprintingTimeWithInventory(0), schedule.getOptimalSprintingTimeWithInventory(1), tol );
        assertEquals( schedule.getOptimalSprintingTimeWithBacklog(1), schedule.getOptimalSprintingTimeWithBacklog(2), tol);

        // Check that the service rate equation is satisfied
        double rho = 0.3 / 1.0;
        double expectedSR = 1.0 / ( 1.0 + 20.0 );
        double actualSR = 1 - ( schedule.getOptimalSprintingTimeWithBacklog(0) / rho ) / schedule.getScheduleCycleTime(); 
        assertEquals( expectedSR, actualSR, tol );
        
        // Check that the cost doesn't change under an equivalent sequence
        ProductionSequence sequence2 = new ProductionSequence(item1, item2, item0);
        OptimalFCyclicSchedule schedule2 = new OptimalFCyclicSchedule(sequence2, 1.0);
        schedule2.compute();
        assertEquals( schedule.getScheduleCost(), schedule2.getScheduleCost(), tol);
        
        // Check that all positions start (in this symmetric sequence) with the same backlog
        assertEquals( schedule.getBacklogPriorToProduction(0), schedule.getBacklogPriorToProduction(1), tol);
        assertEquals( schedule.getBacklogPriorToProduction(1), schedule.getBacklogPriorToProduction(2), tol);

        // For item 0, the starting (-)backlog should equal the starting surplus minus the depletion during 1 setup
        assertEquals( - schedule.getBacklogPriorToProduction(0),
                schedule.getSurplusPriorToFirstSetup(item0) -
                    item0.getDemandRate() * item0.getSetupTime(), tol);

        // For item 1, the starting backlog at position 1 should be equal to the initial surplus minus one run and 2 setups
        assertEquals( - schedule.getBacklogPriorToProduction(1), 
                    schedule.getSurplusPriorToFirstSetup(item1) - 
                        item1.getDemandRate() * ( item0.getSetupTime() + item1.getSetupTime() +
                                schedule.getOptimalSprintingTimeWithBacklog(0) +
                                    schedule.getOptimalCruisingTime(0) +
                                        schedule.getOptimalSprintingTimeWithInventory(0) ), tol);

        // For item 2, the starting backlog at position 2 should equal initial surplus minus 2 runs and 3 setups
        assertEquals( - schedule.getBacklogPriorToProduction(2),
                schedule.getSurplusPriorToFirstSetup(item2) -
                    item2.getDemandRate() * ( item0.getSetupTime() + item1.getSetupTime() + item2.getSetupTime() +
                            schedule.getOptimalSprintingTimeWithBacklog(0) +
                            schedule.getOptimalSprintingTimeWithBacklog(1) +
                            schedule.getOptimalCruisingTime(0) +
                            schedule.getOptimalCruisingTime(1) +
                            schedule.getOptimalSprintingTimeWithInventory(0) +
                            schedule.getOptimalSprintingTimeWithInventory(1) ), tol );

    }
    
    @Test
    public void testNonSymmetricSystem() throws Exception {

        ParamsBuilder paramsBuilder = Params.builderWithDefaults();
        paramsBuilder
            .numItems(3)
            .surplusTargets(c(0.0, 0.0, 0.0))
            .initialDemand(c(0.0, 20.0, 30.0))
            .backlogCosts(c( 19.,  19.,  19. ))
            .inventoryHoldingCosts(c( 1.0, 1.0, 1.0 ))
            .productionRates(c( 0.36630037,  0.36630037,  0.36630037 ))
            .demandRates(c( 0.03333333,  0.13333333,  0.13333333 ))
            .setupTimes(c( 7.77777778,  1.11111111,  1.11111111 ));

        Params params = paramsBuilder.build();
        Item item0 = new Item(0, params);
        Item item1 = new Item(1, params);
        Item item2 = new Item(2, params);
        ProductionSequence sequence = new ProductionSequence(item0, item1, item2);

        OptimalFCyclicSchedule schedule = new OptimalFCyclicSchedule(sequence, 1.0);
        double tol = 1e-5;
        schedule.compute();
        assertEquals( 5.24599356, schedule.getScheduleCost(), tol);
        assertConsistencyInitialSurplus(schedule, 1, tol);

        ProductionSequence sequence2 = new ProductionSequence(item0, item1, item2, item1, item2);
        OptimalFCyclicSchedule schedule2 = new OptimalFCyclicSchedule(sequence2, 1.0);
        schedule2.compute();
        assertEquals( 3.9071442, schedule2.getScheduleCost(), tol);
        assertConsistencyInitialSurplus(schedule2, 1, tol);

        ProductionSequence sequence3 = new ProductionSequence(item0, item1, item2, item1, item2, item1, item2, item1);
        OptimalFCyclicSchedule schedule3 = new OptimalFCyclicSchedule(sequence3, 1.0);
        schedule3.compute();
        assertEquals( 3.76394047, schedule3.getScheduleCost(), tol);
        assertConsistencyInitialSurplus(schedule3, 1, tol);

        ProductionSequence sequence4 = new ProductionSequence(item0, item1, item2, item1, item2);
        OptimalFCyclicSchedule schedule4 = new OptimalFCyclicSchedule(sequence4, 0.91);
        schedule4.compute();
        assertEquals( 6.62526979, schedule4.getScheduleCost(), tol);
        assertConsistencyInitialSurplus(schedule4, 0.91, tol);

    }

    /**
     * This instance was causing the solver to fail.
     * @throws Exception
     */
    @Test
    public void testProblematicInstance() throws Exception {
        
        ParamsBuilder paramsBuilder = Params.builderWithDefaults();
        paramsBuilder
            .numItems(3)
            .surplusTargets(c(0.0, 0.0, 0.0))
            .initialDemand(c(0.0, 20.0, 30.0))
            .backlogCosts(c( 19.,  19.,  19. ))
            .inventoryHoldingCosts(c( 1.0, 1.0, 1.0 ))
            .productionRates(c( 0.36630037,  0.36630037,  0.36630037 ))
            .demandRates(c( 0.03333333,  0.13333333,  0.13333333 ))
            .setupTimes(c( 7.77777778,  1.11111111,  1.11111111 ));

        Params params = paramsBuilder.build();
        Item item0 = new Item(0, params);
        Item item1 = new Item(1, params);
        Item item2 = new Item(2, params);
        
        ProductionSequence sequence = new ProductionSequence(item0, item1, item2, item1, item2, item1, item2);
        OptimalFCyclicSchedule schedule = new OptimalFCyclicSchedule(sequence, 0.91);
        schedule.compute();
    }

    /**
     * Checks that the inventory at the end of the sequence for the last item equals the starting surplus at the
     * beginning of the sequence, and the first item's starting backlog  is the initial inventory minus the loss
     * during the setup. Checks that the starting backlog is consistent with the sprinting times for all positions.
     */
    public static void assertConsistencyInitialSurplus(OptimalFCyclicSchedule schedule, double machEff, double tol) {
        Item lastItem = schedule.getSequence().getLast();
        int lastPosition = schedule.getSequence().getSize() - 1;
        assertEquals( schedule.getSurplusPriorToFirstSetup(lastItem),
                schedule.getOptimalSprintingTimeWithInventory(lastPosition) *
                    ( machEff * lastItem.getProductionRate() - lastItem.getDemandRate() ), tol);

        Item firstItem = schedule.getSequence().getFirst();
        assertEquals( - schedule.getBacklogPriorToProduction(0),
                schedule.getSurplusPriorToFirstSetup(firstItem) - firstItem.getSetupTime() * firstItem.getDemandRate(), tol);

        for ( int n = 0; n < schedule.getSequence().getSize(); n++ ) {
            Item item = schedule.getSequence().getItemAtPosition(n);
            assertEquals( schedule.getBacklogPriorToProduction(n),
                    schedule.getOptimalSprintingTimeWithBacklog(n) * ( item.getProductionRate() * machEff - item.getDemandRate()), tol);
        }

    }
}
