package processes.generators;

import java.util.*;

import org.apache.log4j.Logger;

public class BinaryDistributedRandomTimeIntervalGenerator implements
		IRandomTimeIntervalGenerator {

	private static Logger logger = Logger
			.getLogger(BinaryDistributedRandomTimeIntervalGenerator.class);

	private double timeInterval1;
	private double timeInterval2;
	private double prob1;
	private Random generator;

	public BinaryDistributedRandomTimeIntervalGenerator(int seed,
			double timeInterval1, double prob1, double timeInterval2) {
		this.timeInterval1 = timeInterval1;
		this.prob1 = prob1;
		this.timeInterval2 = timeInterval2;

		this.generator = new Random(seed);
		logger.debug("Initilizing a generator with seed " + seed + ", int1: "
				+ timeInterval1 + " wp: " + prob1 + ", int2: " + timeInterval2
				+ " wp: " + (1 - prob1));
	}

	@Override
	public double nextTimeInterval() {
		if (generator.nextDouble() < prob1) {
			logger.trace("Generator below " + prob1 + " returning "
					+ timeInterval1);
			return timeInterval1;
		} else {
			logger.trace("Generator at or above " + prob1 + " returning "
					+ timeInterval2);
			return timeInterval2;
		}
	}
	
	@Override
	public void warmUp(int cycles) {
		for (int i=0; i<cycles; i++){
			generator.nextInt();
		}
	}

}
