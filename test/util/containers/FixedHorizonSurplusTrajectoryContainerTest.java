package util.containers;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import util.SimBasicTest;

public class FixedHorizonSurplusTrajectoryContainerTest extends SimBasicTest {

	@Test
	public void testAddPoint() {
		double timeHorizon = 9;
		double[] surplusTargets = new double[]{0.0, 0.0, 0.0}; 
		ISurplusTrajectoryContainer container = new FixedHorizonSurplusTrajectoryContainer(timeHorizon, surplusTargets);
		assertNull("Earliest time should be null when there are no points", container.getEarliestTime());
		assertNull("Latest time should be null when there are no points", container.getLatestTime());
		for (int t=0; t < 20; t++) {
			container.addPoint(t, new double[]{-t, -t, -t});
		}
		assertTrue("We should get the last 10 points", container.getEarliestTime() > 9 && container.getEarliestTime() < 11);
		assertTrue("We should get the last 10 points", container.getLatestTime() > 18 && container.getLatestTime() < 20);
	}
	
	@Test
	public void testGetInterpolatedSurplus() {
		ISurplusTrajectoryContainer container = new FixedHorizonSurplusTrajectoryContainer(20, new double[]{0.0, 0.0});
		double tol = 1e-6;
		container.addPoint(10, new double[]{-5.0, 0.0});
		container.addPoint(20, new double[]{0.0, -4});
		assertNull("We cannot interpolate below the earliestTime", container.getInterpolatedSurplus(5));
		assertNull("We cannot interpolate past the latestTime", container.getInterpolatedSurplus(30));
		assertTrue("Interpolated value should be -2.5", 
				Math.abs(container.getInterpolatedSurplus(15)[0] + 2.5) < tol);
		assertTrue("Interpolated value should be -3",
				Math.abs(container.getInterpolatedSurplus(17.5)[1] + 3.0) < tol);
		
	}
	
	@Test
	public void testGetSurplusDeviationArea() {
		ISurplusTrajectoryContainer container = new FixedHorizonSurplusTrajectoryContainer(20, new double[]{0.0, 0.0});
		double tol = 1e-6;
		
		assertTrue("Area should be 0 when there are no points", container.getSurplusDeviationArea()[0] < tol);
		assertTrue("Area should be 0 when there are no points", container.getSurplusDeviationArea()[1] < tol);
		
		container.addPoint(0, new double[]{-5.0, 0.0});
		assertTrue("Area should be 0 when there is 1 point", container.getSurplusDeviationArea()[0] < tol);
		assertTrue("Area should be 0 when there is 1 point", container.getSurplusDeviationArea()[1] < tol);
		
		container.addPoint(10, new double[]{0.0, -4.0});
		assertTrue("Area for item 0 should be 25", Math.abs(container.getSurplusDeviationArea()[0] - 25) < tol);
		assertTrue("Area for item 1 should be 20", Math.abs(container.getSurplusDeviationArea()[1] - 20) < tol);
		
		container.addPoint(20, new double[]{-10.0, 0.0});
		assertTrue("Area for item 0 should be 75", Math.abs(container.getSurplusDeviationArea()[0] - 75) < tol);
		assertTrue("Area for item 1 should be 40", Math.abs(container.getSurplusDeviationArea()[1] - 40) < tol);		
	}
	
	@Test
	public void testGetTime() {
		ISurplusTrajectoryContainer container = new FixedHorizonSurplusTrajectoryContainer(20, new double[]{0.0, 0.0});
		for (int t=0; t<100; t++){
			container.addPoint(t, new double[]{0.0, 0.0});
		}				
		assertTrue("Time range of container should not exceed 20", container.getLatestTime() - container.getEarliestTime() <= 20.1);
	}
	
	
}


