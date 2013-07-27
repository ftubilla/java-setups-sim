package metrics;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import sim.Sim;
import system.Item;
import system.Machine;
import system.MachineSnapshot;
import discreteEvent.AfterEventListener;
import discreteEvent.Event;

public class AverageSurplusMetrics {
	private static Logger logger = Logger.getLogger(AverageSurplusMetrics.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();
	private boolean trace = logger.isTraceEnabled();
	
	private Map<Item,Double> averageDeviation;
	private Machine machine;
	private MachineSnapshot lastMachineSnapshot;
	
	public AverageSurplusMetrics(Sim sim){
		averageDeviation = new HashMap<Item,Double>();
		machine = sim.getMachine();
		for (Item item : machine){
			averageDeviation.put(item, 0.0);
		}		
	
		Event.addAfterEventListener(new AfterEventListener(){
			@Override
			public void execute(Event event, Sim sim) {
				if (Sim.TIME_TO_START_RECORDING && lastMachineSnapshot!=null){
					if (trace){logger.trace("Recording average surplus deviation costs");}
					for (Item item : machine){
						double yb = item.getSurplusDeviation();
						double ya = lastMachineSnapshot.getSurplusDeviation(item);
						double tb = Sim.time();
						double ta = lastMachineSnapshot.getSnapshotTime();
						double area = Math.min(ya, yb)*(tb-ta)+0.5*(tb-ta)*Math.abs(yb-ya);
						averageDeviation.put(item, averageDeviation.get(item) + area);
					}
				}				
				lastMachineSnapshot = machine.getSnapshot();
			}			
		});		
	}
	
	public double getAverageSurplusDeviation(Item item){		
		return averageDeviation.get(item)/(Sim.time()-Sim.METRICS_INITIAL_TIME);		
	}
		
}


