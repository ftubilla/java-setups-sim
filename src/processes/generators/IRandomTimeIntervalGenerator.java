package processes.generators;

/**
 * Main interface for obtaining random intervals. Each implementor should have
 * some initialization parameters such as the random seed and prob. distribution
 * parameters.
 * 
 * @author ftubilla
 * 
 */
public interface IRandomTimeIntervalGenerator {

	public double nextTimeInterval();
	
	/**
	 * Should be call at the set up portion of the sim to cycle the generators a few
	 * times before using them.
	 * 
	 * @param cycles
	 */
	public void warmUp(int cycles);
	
}
