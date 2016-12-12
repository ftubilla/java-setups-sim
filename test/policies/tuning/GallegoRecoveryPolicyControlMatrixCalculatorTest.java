package policies.tuning;

import static util.UtilMethods.c;

import org.junit.Test;

import params.Params;
import params.Params.ParamsBuilder;
import sequences.ProductionSequence;
import system.Item;
import util.SimBasicTest;

public class GallegoRecoveryPolicyControlMatrixCalculatorTest extends SimBasicTest {

    @Test
    public void test() throws Exception {

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
        
        GallegoRecoveryPolicyControlMatrixCalculator calculator = new GallegoRecoveryPolicyControlMatrixCalculator(params);
        ProductionSequence sequence = new ProductionSequence(item0, item1, item2, item1, item2);
        calculator.compute(sequence, false, 1e-5);
        
        // Now compute with a very low tolerance and check that an error is thrown
        boolean errorThrown = false;
        try {
            calculator.compute(sequence, false, 1e-100);
        } catch( Exception e ) {
            errorThrown = true;
        }
        assertTrue( errorThrown );

    }

    @Test
    public void testGallegosExample() throws Exception {

        double[][] expectedG = {
                { 0.008005,  0.023333, -0.056388,  0.191382, -0.018039},
                { 0.010040,  0.026552, -0.056557,  0.032556,  0.182747},
                { 0.181386,  0.010007, -0.051487,  0.029222,  0.006205},
                {-0.054597,  0.147695, -0.203368, -0.035185, -0.065656},
                { 0.033625,  0.030715,  0.003266,  0.025340,  0.059093},
                { 0.039006,  0.034027,  0.007473,  0.022310,  0.031044},
                { 0.018261,  0.030434, -0.041354,  0.006845,  0.026886},
                { 0.105283,  0.128406,  0.217835,  0.111230,  0.104626}
        };
        double tol = 1e-5;
        
        ParamsBuilder builder = Params.builderWithDefaults();
        builder
            .numItems(5)
            .surplusTargets(c(0, 0, 0, 0, 0))
            .initialDemand(c(0, 0, 0, 0, 0))
            .demandRates(c(1, 1, 1, 1, 1))
            .productionRates(c(6, 5, 5, 6, 6))
            .inventoryHoldingCosts(c(57, 77, 55, 73, 72))
            .backlogCosts(c(16495, 10613, 46907, 39997, 38774))
            .setupTimes(c(1, 4, 3, 2, 2));
        
        Params params = builder.build();
        Item item0 = new Item(0, params);
        Item item1 = new Item(1, params);
        Item item2 = new Item(2, params);
        Item item3 = new Item(3, params);
        Item item4 = new Item(4, params);
        
        GallegoRecoveryPolicyControlMatrixCalculator calculator = new GallegoRecoveryPolicyControlMatrixCalculator(params);
        ProductionSequence sequence = new ProductionSequence(item3, item4, item0, item1, item3, item4, item0, item2);
        double[][] G = calculator.compute(sequence, false, 1e-7);
        
        for ( int i = 0; i < G.length; i++ ) {
            for ( int j = 0; j < G[0].length; j++ ) {
                assertEquals(String.format("Entry %d, %d was %.10f expected %.10f",
                        i, j, G[i][j], expectedG[i][j]),
                        expectedG[i][j],
                        G[i][j],
                        tol);
            }
        }
        
        
    }
    
}
