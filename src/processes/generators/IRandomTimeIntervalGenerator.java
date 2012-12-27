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
	
}
