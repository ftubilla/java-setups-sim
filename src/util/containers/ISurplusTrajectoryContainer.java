package util.containers;

/**
 * A trajectory container holds a series of surplus trajectories for computing
 * running means or other statistics.
 * 
 * @author ftubilla
 *
 */
public interface ISurplusTrajectoryContainer {

	/**
	 * Adds a surplus point.
	 * 
	 * @param time
	 * @param surplus
	 */
	public void addPoint(double time, double[] surplus);
	
	/**
	 * Returns the linearly-interpolated surplus value at the given time.
	 * If the given time is earlier or later than the current time range,
	 * returns <code>null</code>.
	 * @param time
	 * @return double[]
	 */
	public Double[] getInterpolatedSurplus(double time);
	
	/**
	 * Returns the total area enclosed by the difference of the surplus
	 * target and the surplus for each item.
	 * @return double[]
	 */
	public double[] getSurplusDeviationArea();

	public Double getEarliestTime();
	
	public Double getLatestTime();

	/**
	 * Returns a copy of the trajectory, useful for modifying and asking "what if" questions.
	 * @return ISurplusTrajectoryContainer
	 */
	public ISurplusTrajectoryContainer copy();
	
}


