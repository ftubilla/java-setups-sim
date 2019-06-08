package discreteEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.Mockito;

import sim.Clock;
import sim.TimeInstant;
import util.SimBasicTest;

public class MasterSchedulerTest extends SimBasicTest {

    @Test
    public void testNextEvent() {
        MasterScheduler masterScheduler = new MasterScheduler(new Clock(0));
        Event event1 = new DummyEvent(1, ScheduleType.CONTROL);
        Event event2 = new DummyEvent(2, ScheduleType.CONTROL);
        masterScheduler.addEvent(event2);
        masterScheduler.addEvent(event1);
        assertEquals( event1, masterScheduler.getNextEvent() );
        assertEquals( new TimeInstant(2), masterScheduler.nextEventTime() );
        assertEquals( DummyEvent.class, masterScheduler.nextEventType() );
        assertFalse( masterScheduler.eventsComplete() );
        assertEquals( event2, masterScheduler.getNextEvent() );
        assertTrue( masterScheduler.eventsComplete() );
    }

    @Test
    public void testDumpAndDelay() {

        Clock clock = Mockito.mock(Clock.class);
        Mockito.when(clock.getTime()).thenReturn(new TimeInstant(0));
        MasterScheduler masterScheduler = new MasterScheduler(clock);
        Event delayableEvent = new DummyEvent(1, ScheduleType.FAILURES);
        Event dumpableEvent = new DummyEvent(2, ScheduleType.CONTROL);
        Event fixedEvent = new DummyEvent(3, ScheduleType.DEMAND);
        masterScheduler.addEvent(fixedEvent);
        masterScheduler.addEvent(delayableEvent);
        masterScheduler.addEvent(dumpableEvent);
        masterScheduler.delayEvents(10);
        masterScheduler.dumpEvents();
        assertEquals( new TimeInstant(3), masterScheduler.nextEventTime() );
        masterScheduler.getNextEvent();
        assertEquals( new TimeInstant(11), masterScheduler.nextEventTime() );
        masterScheduler.getNextEvent();
        assertTrue( masterScheduler.eventsComplete() );

        // Now test hold and delay
        Event delayableEvent2 = new DummyEvent(4, ScheduleType.FAILURES);
        masterScheduler.addEvent(delayableEvent2);
        masterScheduler.holdDelayableEvents();
        Mockito.when(clock.getTime()).thenReturn(new TimeInstant(40));
        masterScheduler.releaseAndDelayEvents();
        assertEquals( new TimeInstant(44), masterScheduler.nextEventTime() );
    }

}
