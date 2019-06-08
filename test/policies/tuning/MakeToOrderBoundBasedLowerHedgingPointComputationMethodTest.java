package policies.tuning;

import static org.junit.Assert.assertEquals;
import static util.UtilMethods.c;

import org.junit.Test;

import params.Params;
import params.Params.ParamsBuilder;
import sim.Sim;
import system.Item;
import util.SimBasicTest;

public class MakeToOrderBoundBasedLowerHedgingPointComputationMethodTest extends SimBasicTest {

    @Test
    public void test() {

        ParamsBuilder paramsBuilder = Params.builder();
        paramsBuilder
            .numItems(3)
            .surplusTargets(c(0.0, 0.0, 0.0))
            .initialDemand(c(0.0, 20.0, 30.0))
            .backlogCosts(c(1.0, 2.0, 3.0))
            .inventoryHoldingCosts(c(1.0, 2.0, 3.0))
            .productionRates(c(2.0, 4.0, 1.0))
            .demandRates(c(0.1, 0.1, 0.1))
            .setupTimes(c(1,1,1));

        Sim sim = getSim(paramsBuilder.build());
        MakeToOrderBoundBasedLowerHedgingPointsComputationMethod method =
                new MakeToOrderBoundBasedLowerHedgingPointsComputationMethod();
        method.compute(sim);
        for ( Item item : sim.getMachine() ) {
            double ystar = sim.getSurplusCostLowerBound().getIdealSurplusDeviation(item.getId());
            double S = item.getSetupTime();
            double d = item.getDemandRate();
            assertEquals( ystar - S * d , item.getSurplusTarget() - method.getLowerHedgingPoint(item), 1e-4 );
        }
    }

}
