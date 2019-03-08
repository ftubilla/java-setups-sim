package sim;

import static org.junit.Assert.*;

import org.junit.Test;

public class TimeInstantTest {

    private static final double TOL = 1e-9;

    @Test
    public void testAdd() {
        TimeInstant t0 = new TimeInstant(0);
        TimeInstant t1 = t0.add(1);
        TimeInstant t2 = t1.add(t1);
        assertNotEquals(t0, t1);
        assertEquals(1.0, t1.doubleValue(), TOL);
        assertNotEquals(t1, t2);
        assertEquals(2.0, t2.doubleValue(), TOL);
    }

    @Test
    public void testSubtract() {
        TimeInstant t0 = new TimeInstant(0);
        TimeInstant t1 = new TimeInstant(1);
        assertEquals(1.0, t1.subtract(t0).doubleValue(), TOL);
        System.out.println(String.format("Time %s", t1));
    }

}
