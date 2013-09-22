package output;

import sim.Sim;

public class SampleRateFilter implements IFilter {
	
	private double samplePeriod;
	private double lastRecordTime = -Double.MAX_VALUE;

	public SampleRateFilter(double samplePeriod){
		this.samplePeriod = samplePeriod;
	}
	
	
	@Override
	public boolean passFilter(Sim sim) {
		boolean passFilter = false;
		Double currentTime = null;
		currentTime = sim.getTime();
		
		if (currentTime - lastRecordTime > samplePeriod){
			passFilter = true;
			lastRecordTime = currentTime;
		}
		
		return passFilter;
	}

}
