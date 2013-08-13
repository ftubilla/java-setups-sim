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
	private Map<Item,Double> averageInventory;
	private Map<Item,Double> averageBacklog;
	private Machine machine;
	private MachineSnapshot lastMachineSnapshot;
	
	public AverageSurplusMetrics(Sim sim){
		
		//Initialize data structures
		averageDeviation = new HashMap<Item,Double>();
		averageInventory = new HashMap<Item,Double>();
		averageBacklog = new HashMap<Item,Double>();
		machine = sim.getMachine();
		for (Item item : machine){
			averageDeviation.put(item, 0.0);
			averageInventory.put(item, 0.0);
			averageBacklog.put(item, 0.0);
		}		
	
		Event.addAfterEventListener(new AfterEventListener(){
			@Override
			public void execute(Event event, Sim sim) {
				if (Sim.TIME_TO_START_RECORDING && lastMachineSnapshot!=null){
					if (trace){logger.trace("Recording average surplus deviation costs");}
					for (Item item : machine){
						
						double tb = Sim.time();
						double ta = lastMachineSnapshot.getSnapshotTime();						
						double area;
						
						//Note that I cannot simply use the inventory and backlog values at the end points, because that
						//would not tell me the crossover time from inventory to backlog or vice-versa as calculated in the method below
						
						//Inventory
						area = findAreaAboveXAxis(ta, lastMachineSnapshot.getSurplus(item), tb, item.getSurplus());
						averageInventory.put(item, averageInventory.get(item)+area);
						
						//Backlog
						area = findAreaBelowXAxis(ta, lastMachineSnapshot.getSurplus(item), tb, item.getSurplus());
						averageBacklog.put(item, averageBacklog.get(item)+area);
						
						//Deviation
						area = findAreaAboveXAxis(ta, lastMachineSnapshot.getSurplusDeviation(item), tb, item.getSurplusDeviation());
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
	
	public double getAverageInventory(Item item){
		return averageInventory.get(item)/(Sim.time()-Sim.METRICS_INITIAL_TIME);
	}
	
	public double getAverageBacklog(Item item){
		return averageBacklog.get(item)/(Sim.time()-Sim.METRICS_INITIAL_TIME);
	}
		
	/**
	 * Computes the area enclosed by the <em>positive</em> segment of the given line and
	 * the x-axis.
	 *
	 */
	private double findAreaAboveXAxis(double x1, double y1, double x2, double y2){
		if (y1 <= 0 && y2 <=0){
			//Both points are below the x-axis
			return 0.0;
		} else {
			//Find the x-crossover point
			double xC = -y1*(x2-x1)/(1.0*(y2-y1))+x1;
			if (y1 <= 0 && y2 > 0){
				//Slope is positive, set point 1 to crossover
				x1= xC;
				y1 = 0.0;
			} else {
				if (y1 > 0 && y2<=0){
					//Slope is negative, set point 2 to crossover
					x2 = xC;
					y2 = 0.0;
				}
			}									
		}
		assert y1>=0 && y2>=0 : "Check the area calculating function!";
		return Math.min(y1, y2)*(x2-x1)+0.5*(x2-x1)*Math.abs(y2-y1);
	}
	
	private double findAreaBelowXAxis(double x1, double y1, double x2, double y2){
		return findAreaAboveXAxis(x1,-y1,x2,-y2);
	}
		
}


