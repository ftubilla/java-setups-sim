package discreteEvent;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import params.Params;
import params.Params.ParamsBuilder;
import sim.Sim;
import sim.TimeInstant;
import util.SimBasicTest;

public class RepairTest extends SimBasicTest {

    @Test
    public void testHandle() {

        ParamsBuilder paramsBuilder = Params.builder();
        paramsBuilder
            .numItems(3)
            .build();

        Sim sim = getSim(paramsBuilder.build());

        Failure failure = new Failure(TimeInstant.at(10));
        failure.handle(sim);
        Event nextEvent = sim.getNextEvent();
        assertTrue( nextEvent instanceof Repair );
        Repair repair = (Repair) nextEvent;
        repair.handle(sim);
        assertTrue( sim.getMachine().isUp() );

    }

}
