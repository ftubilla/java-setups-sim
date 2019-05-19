package discreteEvent;

import static org.junit.Assert.assertEquals;
import static util.UtilMethods.c;

import org.junit.Test;

import params.Params;
import params.Params.ParamsBuilder;
import sim.Sim;
import sim.TimeInstant;
import util.SimBasicTest;

public class EventTest extends SimBasicTest {

    @Test
    public void testHandle() {
        
        ParamsBuilder paramsBuilder = Params.builder();
        paramsBuilder
            .numItems(3)
            .surplusTargets(c(0.0, 0.0, 0.0))
            .initialDemand(c(0.0, 20.0, 30.0))
            .backlogCosts(c(1.0, 2.0, 3.0))
            .inventoryHoldingCosts(c(1.0, 2.0, 3.0))
            .productionRates(c(2.0, 4.0, 1.0))
            .demandRates(c(0.1, 0.1, 0.1))
            .setupTimes(c(10,20,30));

        Sim sim = getSim(paramsBuilder.build());

        ControlEvent event = new ControlEvent(TimeInstant.at(10));
        event.handle(sim);

        assertEquals( TimeInstant.at(10), sim.getTime() );
        assertEquals( event, sim.getLatestEvent() );

    }

}
