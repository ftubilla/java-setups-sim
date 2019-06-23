package lowerbounds;

import static org.junit.Assert.assertEquals;
import static util.UtilMethods.c;

import org.junit.Test;

import params.Params;
import util.SimBasicTest;

public class FrequencyRatioLimitedSurplusCostHeuristicBoundTest extends SimBasicTest {

    @Test
    public void test() throws Exception {
        Params params = Params.builder()
                .demandRates(c(1.0, 1.0, 1.0))
                .productionRates(c(10, 10, 10))
                .setupTimes(c(10, 10, 10))
                .backlogCosts(c(1, 1, 1e-6))
                .inventoryHoldingCosts(c(1, 1, 1e-6))
                .build();
        SurplusCostLowerBound lowerBound = new SurplusCostLowerBound("lower bound", params);
        lowerBound.compute();
        assertEquals( 1.0, lowerBound.getSetupFreq().get(2).getSol() / lowerBound.getSetupFreq().get(0).getSol() * 1e3, 0.1 );

        FrequencyRatioLimitedSurplusCostHeuristicBound heuristicBound = new FrequencyRatioLimitedSurplusCostHeuristicBound("heur bound", params);
        heuristicBound.compute();
        assertEquals( 1.0, heuristicBound.getSetupFreq().get(2).getSol() / heuristicBound.getSetupFreq().get(0).getSol() * 2, 0.1 );
    }

}
