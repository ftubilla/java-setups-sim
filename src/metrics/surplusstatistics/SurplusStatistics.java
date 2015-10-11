package metrics.surplusstatistics;

public interface SurplusStatistics {

	public double getInitialTime();
	
	public double getFinalTime();
	
	public double getAverageInventory();
	
	public double getAverageBacklog();

	public double getServiceLevel();
	
	public double getMinSurplus();

}
