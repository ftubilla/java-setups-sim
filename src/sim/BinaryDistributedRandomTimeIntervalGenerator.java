package sim;

import java.util.*;

public class BinaryDistributedRandomTimeIntervalGenerator implements IRandomTimeIntervalGenerator {

	private double timeInterval1;
	private double timeInterval2;
	private double prob1;
	private Random generator;
	
	public BinaryDistributedRandomTimeIntervalGenerator(int seed, double timeInterval1, double prob1, double timeInterval2){
		this.timeInterval1 = timeInterval1;
		this.prob1 = prob1;
		this.timeInterval2 = timeInterval2;
		
		this.generator = new Random(seed);
		
	}
	
	
	@Override
	public double nextTimeInterval() {
		if (generator.nextDouble()< prob1){
			return timeInterval1;
		} else {
			return timeInterval2;
		}
	}

}
