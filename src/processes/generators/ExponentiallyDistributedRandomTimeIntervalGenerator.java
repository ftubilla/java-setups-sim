package processes.generators;

import org.apache.log4j.Logger;

public class ExponentiallyDistributedRandomTimeIntervalGenerator implements
		IRandomTimeIntervalGenerator {

	private static Logger logger = Logger
			.getLogger(ExponentiallyDistributedRandomTimeIntervalGenerator.class);

	private double mean;
	private MersenneTwisterFast generator;

	public ExponentiallyDistributedRandomTimeIntervalGenerator(long seed,
			double mean) {
		this.mean = mean;
		this.generator = new MersenneTwisterFast(seed);
		logger.debug("Initializing generator with mean " + mean + " and seed "
				+ seed);
	}

	@Override
	public double nextTimeInterval() {
		return -mean * Math.log(1 - generator.nextDouble());
	}
	
	@Override
	public void warmUp(int cycles) {
		for (int i=0; i<cycles; i++){
			generator.nextInt();
		}
	}

}
