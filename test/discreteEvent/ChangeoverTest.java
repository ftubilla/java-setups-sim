package discreteEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static util.UtilMethods.c;

import org.junit.Test;

import params.Params;
import params.Params.ParamsBuilder;
import sim.Sim;
import sim.TimeInstant;
import system.Machine;
import util.SimBasicTest;

public class ChangeoverTest extends SimBasicTest {

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

        // Schedule a failure at time 2
        Failure failure = new Failure(TimeInstant.at(2.0));
        sim.getMasterScheduler().addEvent(failure);

        Changeover changeover = new Changeover(TimeInstant.at(1.0), sim.getMachine().getItemById(1));
        assertEquals( TimeInstant.at(1.0), changeover.getTime() );
        assertFalse(sim.getMachine().isChangingSetups());
        changeover.handle(sim);
        assertTrue(sim.getMachine().isChangingSetups());
        assertEquals(Machine.OperationalState.SETUP, sim.getMachine().getOperationalState());
        Event setupComplete = sim.getNextEvent();
        setupComplete.handle(sim);
        assertEquals( TimeInstant.at(21.0), setupComplete.getTime() );
        assertEquals(sim.getMachine().getItemById(1), sim.getMachine().getSetup());
        assertFalse(sim.getMachine().isChangingSetups());

        // The failure should have been pushed to time 22 (because of the 20 time units of setup change)
        while ( !sim.getMasterScheduler().eventsComplete() ) {
            Event nextEvent = sim.getMasterScheduler().getNextEvent();
            if ( nextEvent.getScheduleType() == ScheduleType.FAILURES ) {
                assertEquals( TimeInstant.at(22.0), nextEvent.getTime() );
            }
        }

    }

}
