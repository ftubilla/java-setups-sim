package system;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import sim.Sim;

public class MachineSnapshot {
	private static Logger logger = Logger.getLogger(MachineSnapshot.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();
	private boolean trace = logger.isTraceEnabled();
	
	private Map<Item,Double> surplus;
	private Map<Item,Double> surplusDeviations;
	private double snapshotTime;
	
	public MachineSnapshot(Machine machine){
		
		if (trace){
			logger.trace("Creating snapshot of the machine at time " + Sim.time());
		}
		snapshotTime = Sim.time();
		surplus = new LinkedHashMap<Item,Double>();
		surplusDeviations = new LinkedHashMap<Item,Double>();
		for (Item item : machine){
			surplus.put(item, item.getSurplus());
			surplusDeviations.put(item, item.getSurplusDeviation());			
		}		
	}
	
	public double getSurplusDeviation(Item item){
		return surplusDeviations.get(item);
	}
	
	public double getSnapshotTime(){
		return snapshotTime;
	}

	public double getSurplus(Item item){
		return surplus.get(item);
	}
	
}


