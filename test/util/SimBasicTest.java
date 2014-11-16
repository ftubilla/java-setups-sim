package util;
import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;


public class SimBasicTest extends TestCase {

	@Before
	public void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("config/log4j.test.properties");		
	}

	public void assertWithinTolerance(double x1, double x2, double tolerance){
		assertTrue(Math.abs(x1 - x2) < tolerance);
	}
	
}


