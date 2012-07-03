package sim.metrics;

import sim.*;


public class Metrics {

	private TimeFractionsMetrics timeFractions;
	
	
	public Metrics(Sim sim){
		
		timeFractions = new TimeFractionsMetrics(sim.getMachine());
		
	}


	public TimeFractionsMetrics getTimeFractions() {
		return timeFractions;
	}
	
	
	
}
