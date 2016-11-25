package sequences;

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

        ProductionSequence sequence2 = new ProductionSequence(item0, item1, item2, item1, item2);
        OptimalFCyclicSchedule schedule2 = new OptimalFCyclicSchedule(sequence2, 1.0);
        schedule2.compute();
        assertEquals( 3.9071442, schedule2.getScheduleCost(), tol);

        ProductionSequence sequence3 = new ProductionSequence(item0, item1, item2, item1, item2, item1, item2, item1);
        OptimalFCyclicSchedule schedule3 = new OptimalFCyclicSchedule(sequence3, 1.0);
        schedule3.compute();
        assertEquals( 3.76394047, schedule3.getScheduleCost(), tol);

    }

}
