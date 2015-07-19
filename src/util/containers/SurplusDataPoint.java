package util.containers;

import lombok.Getter;

@Getter
public class SurplusDataPoint implements Comparable<SurplusDataPoint> {

	private static final double[] EMPTY_SURPLUS = new double[]{};
	
	private final double time;
	private final double[] surplus;
	private final String toString;
	
	/**
	 * Creates a data point with empty surplus, useful for comparing the time
	 * keys in other data points.
	 * @param time
	 */
	protected SurplusDataPoint(final double time){
		this(time, EMPTY_SURPLUS);
	}
	
	public SurplusDataPoint(final double time, final Double[] surplus){
		this(time, toArray(surplus));
	}
	
	public SurplusDataPoint(final double time, final double[] surplus){
		this.time = time;
		this.surplus = surplus;
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("Time %.3f Surplus ", time));
		for (double d : surplus){
			sb.append(String.format("%.3f ", d));
		}
		toString = sb.toString();
	}	
	
	@Override
	public int compareTo(SurplusDataPoint dataPoint) {
		return Double.compare(this.time, dataPoint.time);
	}	
	
	@Override
	public String toString(){
		return toString;
	}
	
	private static double[] toArray(final Double[] input){
		double[] output = new double[input.length];
		for (int i=0; i<output.length; i++){
			output[i] = input[i];
		}
		return output;
	}
	
}


