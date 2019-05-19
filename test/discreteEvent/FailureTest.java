package discreteEvent;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import params.Params;
import params.Params.ParamsBuilder;
import sim.Sim;
import sim.TimeInstant;
import util.SimBasicTest;

public class FailureTest extends SimBasicTest {

    @Test
    public void test() {

        ParamsBuilder paramsBuilder = Params.builder();
        paramsBuilder
            .numItems(3)
            .build();

        Sim sim = getSim(paramsBuilder.build());

        Failure failure = new Failure(TimeInstant.at(10));
        failure.handle(sim);
        assertTrue( sim.getMachine().isDown() );
        assertTrue( sim.getNextEvent() instanceof Repair );
    }

}
