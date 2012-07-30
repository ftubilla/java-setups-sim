package sim;

import java.util.*;

public class ExponentiallyDistributedRandomTimeIntervalGenerator implements IRandomTimeIntervalGenerator {

	private double mean;
	private double prob1;
	private Random generator;
	
	public ExponentiallyDistributedRandomTimeIntervalGenerator(int seed, double mean){
		this.mean = mean;
		this.generator = new Random(seed);
		
	}
	
	
	@Override
	public double nextTimeInterval() {
		return -mean*Math.log(1-generator.nextDouble());
	}

}
