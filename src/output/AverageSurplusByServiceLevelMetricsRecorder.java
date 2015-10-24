package output;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import metrics.AverageSurplusByServiceLevelMetrics;
import metrics.surplusstatistics.SurplusStatistics;
import sim.Sim;
import system.Item;

public class AverageSurplusByServiceLevelMetricsRecorder extends Recorder {
	private static Logger logger = Logger.getLogger(AverageSurplusByServiceLevelMetricsRecorder.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();

	@SuppressWarnings("unused")
	private boolean trace = logger.isTraceEnabled();
	
	enum Column {SIM_ID, METRIC, ITEM, VALUE};
	
	public AverageSurplusByServiceLevelMetricsRecorder(){
		super("output/average_surplus_by_service_level_metrics.txt");
		super.writeHeader(Column.class);		
	}
	
	@Override
	public void recordEndOfSim(Sim sim){
		
		AverageSurplusByServiceLevelMetrics metrics = sim.getMetrics().getAverageSurplusByServiceLevelMetrics();
		Object[] row = new Object[4];
		for (Item item : sim.getMachine()){
			
			row[0] = sim.getId();
 			row[2] = item.getId();

 			double desiredServiceLevel = sim.getDerivedParams().getServiceLevels().get(item.getId());
 			Pair<Double, SurplusStatistics> pair = metrics.findOptimalOffsetForServiceLevel(item, desiredServiceLevel);
 			
 			row[1] = "DESIRED_SERVICE_LEVEL";
 			row[3] = desiredServiceLevel;
 			record(row);
 			
 			row[1] = "OPTIMAL_SURPLUS_TARGET";
 			row[3] = pair.getLeft();
 			record(row);
 			
 			SurplusStatistics stats = pair.getRight();
 			row[1] = "INVENTORY";
 			row[3] = stats.getAverageInventory();
 			record(row);

			row[1] = "BACKLOG";
			row[3] = stats.getAverageBacklog();
			record(row);
			
			row[1] = "SERVICE_LEVEL";
			row[3] = stats.getServiceLevel();
			record(row);
			
		}
		
	}
	
}


