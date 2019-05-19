package discreteEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.Mockito;

import sim.Clock;
import sim.TimeInstant;
import util.SimBasicTest;

public class ScheduleTest extends SimBasicTest {

    @Test
    public void testDelayableSchedule() {

        Schedule schedule = new Schedule(ScheduleType.FAILURES, new Clock(0));

        Event event1 = new Failure(TimeInstant.at(1));
        Event event2 = new Failure(TimeInstant.at(2));
        schedule.addEvent(event1);
        schedule.addEvent(event2);
        assertEquals(event1, schedule.peekNextEvent());
        schedule.delayEvents(TimeInstant.at(10));

        assertEquals(TimeInstant.at(11), schedule.getNextEvent().getTime());
        assertEquals(TimeInstant.at(12), schedule.getNextEvent().getTime());
        assertEquals(TimeInstant.at(1), schedule.getLastInterEventTime());

    }

    @Test
    public void testHoldableSchedule() {

        Clock clock = Mockito.mock(Clock.class);
        Mockito.when(clock.getTime()).thenReturn(TimeInstant.at(0));
        Schedule schedule = new Schedule(ScheduleType.FAILURES, clock);

        Event event1 = new Failure(TimeInstant.at(1));
        Event event2 = new Failure(TimeInstant.at(2));
        schedule.addEvent(event1);
        schedule.addEvent(event2);
        schedule.holdEvents();
        assertTrue( schedule.isOnHold() );
        // Advance the mocked clock by 10
        Mockito.when(clock.getTime()).thenReturn(TimeInstant.at(10));
        schedule.releaseAndDelayEvents();
        assertEquals(TimeInstant.at(11), schedule.getNextEvent().getTime());
        assertEquals(TimeInstant.at(12), schedule.getNextEvent().getTime());
        assertEquals(TimeInstant.at(1), schedule.getLastInterEventTime());

    }

}
