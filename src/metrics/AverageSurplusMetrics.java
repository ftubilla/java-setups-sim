package metrics;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import sim.Clock;
import sim.Sim;
import system.Item;
import system.Machine;
import system.MachineSnapshot;
import discreteEvent.Event;
import discreteEvent.EventListener;

public class AverageSurplusMetrics {
	private static Logger logger = Logger.getLogger(AverageSurplusMetrics.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();
	private boolean trace = logger.isTraceEnabled();
	
	private Map<Item, Double> averageDeviation;
	private Map<Item, Double> averageInventory;
	private Map<Item, Double> averageBacklog;
	private Map<Item, Double> averageServiceLevel;
	private Machine machine;
	private MachineSnapshot lastMachineSnapshot;
	private Clock clock;
	
	public AverageSurplusMetrics(Sim sim){
		
		this.clock = sim.getClock();
		
		//Initialize data structures
		averageDeviation = new HashMap<Item, Double>();
		averageInventory = new HashMap<Item, Double>();
		averageBacklog = new HashMap<Item, Double>();
		averageServiceLevel = new HashMap<Item, Double>();
		machine = sim.getMachine();
		for (Item item : machine){
			averageDeviation.put(item, 0.0);
			averageInventory.put(item, 0.0);
			averageBacklog.put(item, 0.0);
			averageServiceLevel.put(item, 0.0);
		}		
	
		sim.getListenersCoordinator().addAfterEventListener(new EventListener(){
			@Override
			public void execute(Event event, Sim sim) {
				if (sim.isTimeToRecordData() && lastMachineSnapshot!=null){
					if (trace){logger.trace("Recording average surplus deviation costs");}
					for (Item item : machine){
						
						double tb = sim.getTime();
						double ta = lastMachineSnapshot.getSnapshotTime();						
						double area;
						double period;
						
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
						
						//Service Level
						period = findPeriodAboveXAxis(ta, lastMachineSnapshot.getSurplus(item), tb, item.getSurplus());
						averageServiceLevel.put(item, averageServiceLevel.get(item) + period);
					}
				}				
				lastMachineSnapshot = machine.getSnapshot();
			}			
		});		
	}
	
	public double getAverageSurplusDeviation(Item item){		
		return averageDeviation.get(item) / clock.getMetricsRecordingTime();		
	}
	
	public double getAverageInventory(Item item){
		return averageInventory.get(item) / clock.getMetricsRecordingTime();
	}
	
	public double getAverageBacklog(Item item){
		return averageBacklog.get(item) / clock.getMetricsRecordingTime();
	}
	
	public double getAverageServiceLevel(Item item) {
		return averageServiceLevel.get(item) / clock.getMetricsRecordingTime();
	}
		
	/**
	 * Computes the area enclosed by the <em>positive</em> segment of the given line and
	 * the x-axis.
	 *
	 */
	protected double findAreaAboveXAxis(double x1, double y1, double x2, double y2){
		if (y1 <= 0 && y2 <=0){
			//Both points are below the x-axis
			return 0.0;
		} else {
			//Find the x-crossover point
			double xC = findYAxisIntersection(x1, y1, x2, y2);
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
		return Math.min( y1, y2 ) * ( x2 - x1 ) + 0.5 * ( x2 - x1 ) * Math.abs( y2 - y1 );
	}
	
	protected double findAreaBelowXAxis(double x1, double y1, double x2, double y2){
		return findAreaAboveXAxis(x1,-y1,x2,-y2);
	}
		
	protected double findPeriodAboveXAxis(double x1, double y1, double x2, double y2) {
		if (y1 <= 0 && y2 <= 0) {
			//Both points are below the x-axis
			return 0.0;
		} 
		if (y1 > 0 && y2 > 0) {
			//Both points are above the x-axis
			return x2 - x1;
		}
		double xC = findYAxisIntersection(x1, y1, x2, y2);
		if (y1 <= 0 && y2 > 0) {
			//Slope is positive
			return x2 - xC;
		} else {
			//Slope is negative
			return xC - x1;
		}
	}
	
	protected double findYAxisIntersection(double x1, double y1, double x2, double y2) {
		return -y1 * ( x2 - x1 ) / ( (double) ( y2 - y1 ) ) + x1;
	}

}

