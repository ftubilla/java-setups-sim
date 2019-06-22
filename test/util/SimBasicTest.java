package util;

import static org.junit.Assert.*;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Ignore;
import org.mockito.MockitoAnnotations;

import output.Recorders;
import params.Params;
import sim.Sim;
import sim.SimSetup;

@Ignore
public class SimBasicTest {

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        PropertyConfigurator.configure("config/log4j.test.properties");
    }

    public void assertWithinTolerance(double x1, double x2, double tolerance) {
        assertTrue(String.format("%.15f should be equal to %.15f within tolerance of %.15f", x1, x2, tolerance),
                Math.abs(x1 - x2) < tolerance);
    }

    /**
     * Creates and sets up a new sim instance for testing.
     * 
     * @param params
     * @return Sim
     */
    protected Sim getSim(Params params) {
        Sim sim = new Sim(params);
        SimSetup.setUp(sim, new Recorders());
        return sim;
    }

}
