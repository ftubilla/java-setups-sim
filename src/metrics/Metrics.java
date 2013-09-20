package metrics;

import sim.*;


public class Metrics {

	private TimeFractionsMetrics timeFractions;
	private AverageSurplusMetrics averageSurplus;
	
	public Metrics(Sim sim){
		timeFractions = new TimeFractionsMetrics(sim);
		averageSurplus = new AverageSurplusMetrics(sim);
	}

	public TimeFractionsMetrics getTimeFractions() {
		return timeFractions;
	}
	
	public AverageSurplusMetrics getAverageSurplusMetrics(){
		return averageSurplus;
	}
		
}
