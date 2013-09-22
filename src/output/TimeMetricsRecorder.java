package output;

import java.util.Map;

import org.apache.log4j.Logger;

import sim.Sim;
import system.Item;
import discreteEvent.Event;

public class TimeMetricsRecorder extends Recorder {

	private static Logger logger = Logger.getLogger(TimeMetricsRecorder.class);
	
	private boolean trace = logger.isTraceEnabled();
	
	public enum Column {SIM_ID,TIME,SETUP,ITEM,SURPLUS,CUM_PROD,CUM_DEM};
	Map<Item, IFilter> itemFilterMap=null;
	
	public TimeMetricsRecorder(){
		super("output/time_metrics.txt");
		super.writeHeader(Column.class);
	}
	
	public TimeMetricsRecorder(Map<Item,IFilter> itemFilterMap){
		this();
		this.itemFilterMap = itemFilterMap;
	}
	
	@Override
	public void recordAfterEvent(Sim sim, Event event) {
		
		if (trace) {logger.trace("Recording state of machine at time " + sim.getTime());}
		
		for (Item item : sim.getMachine()){
			//TODO Improve how this is done
			boolean passFilter = true;
			if (itemFilterMap != null){
				passFilter = itemFilterMap.get(item).passFilter(sim);
			}
			if (passFilter){
				record(sim.getId() + " " + sim.getTime() + " "  + sim.getMachine().getSetup().getId() + " " + item.getId() + " " + item.getSurplus() + 
						" " + item.getCumulativeProduction() + " " + item.getCumulativeDemand());
			}
		}
	}
	

}

	


