package lowerbounds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import optimization.OptimizationVar;
import optimization.Posynomial;
import params.Params;
import params.ParamsFactory;
import util.SimBasicTest;

public class SurplusCostLowerBoundTest extends SimBasicTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testSampleInstances() throws Exception {
        ParamsFactory factory = new ParamsFactory("test_inputs");
        Collection<Params> paramsCollection = factory.make();
        double tol = 1e-3;
        for ( Params params : paramsCollection ) {
            SurplusCostLowerBound lowerBound = new SurplusCostLowerBound("", params);
            lowerBound.compute();
            Posynomial obj = lowerBound.getUnscaledObjectivePosynomial(lowerBound.params, lowerBound.setupFreq, lowerBound.transitionFreq,
                    lowerBound.cruisingFrac, lowerBound.scalingFactor);
            Map<OptimizationVar, Double> values = new HashMap<>();
            Consumer<OptimizationVar> addVar = x -> values.put(x, x.getSol());
            lowerBound.setupFreq.getOptimizationVars().forEach(addVar);
            lowerBound.cruisingFrac.getOptimizationVars().forEach(addVar);
            lowerBound.transitionFreq.getOptimizationVars().forEach(addVar);
            // Evaluate the objective value
            double objectiveValue = obj.eval(values);
            // Compare with the expected value after unscaling the variables
            double expectedObjectiveValue = 0.0;
            double capConstraintLHS = 0;
            double capConstraintRHS = lowerBound.scalingFactor;
            double rho = 0;
            for ( int i = 0; i < params.getNumItems(); i++ ) {
                double di = params.getDemandRates().get(i);
                double mui = params.getProductionRates().get(i);
                double e = params.getMachineEfficiency();
                double hi = params.getInventoryHoldingCosts().get(i);
                double bi = params.getBacklogCosts().get(i);
                double ni = lowerBound.setupFreq.get(i).getSol() * lowerBound.scalingFactor;
                double pci = lowerBound.cruisingFrac.get(i).getSol() * lowerBound.scalingFactor;
                double Si = params.getSetupTimes().get(i);
                rho += di / mui;
                // Add the term for the objective function
                expectedObjectiveValue += 0.5 * di * ( 1 - di / ( mui * e ) ) * hi * bi * Math.pow(1 - pci, 2) / ( hi + bi ) / ni;
                // Check the total transition frequency into and out of item i
                double transitionIntoI = 0.0;
                double transitionOutOfI = 0.0;
                for ( int j = 0; j < params.getNumItems(); j++ ) {
                    if ( i != j ) {
                        transitionIntoI += lowerBound.transitionFreq.get(j, i).getSol() * lowerBound.scalingFactor;
                        transitionOutOfI += lowerBound.transitionFreq.get(i, j).getSol() * lowerBound.scalingFactor;
                        assertEquals("The stored transition is not correct", lowerBound.getTransitionFreq(i, j),
                                lowerBound.transitionFreq.get(i,j).getSol() * lowerBound.scalingFactor, tol);
                    }
                }
                assertEquals("The transition freq into i constraint is not correct",
                        transitionIntoI, ni, tol );
                assertEquals("The transition freq out of i constraint is not correct",
                        transitionOutOfI, ni, tol );
                // Add the term for the capacity constraint
                capConstraintLHS += ni * Si + pci * ( 1 - di / (mui * e));
                assertEquals("The stored transition is not correct", ni, lowerBound.getIdealFrequency(i), tol);
                assertEquals("The stored cruising fraction is not correct", pci, lowerBound.getCruisingFrac(i), tol);
                assertEquals("The ideal peak deviation is not correct",
                        di * ( 1 - (di / mui) / e ) * ( 1 - pci ) / ni,
                        lowerBound.getIdealSurplusDeviation(i), tol);
            }
            assertEquals("The cost function optimized is not correct", expectedObjectiveValue, objectiveValue, tol);
            assertEquals("The objective value stored is not correct", expectedObjectiveValue, lowerBound.getLowerBound(), tol);
            assertEquals("The capacity constraint is not correct", capConstraintLHS, capConstraintRHS, tol);
            assertEquals("The scaling factor is not correct", 1 - rho / params.getMachineEfficiency(), lowerBound.scalingFactor, tol);
        }
    }

    @Test
    public void testIsCruisingFlag() throws Exception {
        Params params = Params.builder()
                                .demandRates(ImmutableList.of(0.1, 0.001, 0.001))
                                .setupTimes(ImmutableList.of(1.0, 100.0, 100.0))
                                .backlogCosts(ImmutableList.of(100.0, 1.0, 1.0))
                                .build();
        SurplusCostLowerBound lowerBound = new SurplusCostLowerBound("J", params);
        lowerBound.compute();
        assertTrue( "The lower bound should prescribe cruising of item 0", lowerBound.getIsCruising());
        assertTrue( "Item 0 should cruise", lowerBound.getCruisingFrac(0) > 0 );
    }

}

