package util;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.mockito.MockitoAnnotations;

import junit.framework.TestCase;


public class SimBasicTest extends TestCase {

	@Before
	public void setUp() throws Exception {
		super.setUp();
		MockitoAnnotations.initMocks(this);
		PropertyConfigurator.configure("config/log4j.test.properties");		
	}

	public void assertWithinTolerance(double x1, double x2, double tolerance){
		assertTrue(String.format("%.15f should be equal to %.15f within tolerance of %.15f",
				x1, x2, tolerance),
				Math.abs(x1 - x2) < tolerance);
	}
	
}


