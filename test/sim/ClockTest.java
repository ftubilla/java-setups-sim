package sim;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ClockTest {

    @Test
    public void testPrecision() {
        Clock clock = new Clock(0);
        clock.advanceClockBy(4e7);
        assertTrue(clock.hasReachedEpoch(4e7));
        assertFalse(clock.hasPassedEpoch(4e7));
        clock.advanceClockBy(1e-10);
        assertTrue(clock.hasReachedEpoch(4e7));
        assertTrue(clock.hasPassedEpoch(4e7));
    }

    @Test
    public void testToString() {
        Clock clock = new Clock(0);
        clock.advanceClockBy(1e2);
        System.out.println(String.format("Clock time is %s", clock.getTime()));
    }

}
