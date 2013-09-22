package metrics;

import java.util.*;

import org.apache.log4j.Logger;

import discreteEvent.BeforeEventListener;
import discreteEvent.Event;
import system.*;
import system.Machine.FailureState;
import sim.*;


public class TimeFractionsMetrics {

	private static Logger logger = Logger.getLogger(TimeFractionsMetrics.class);
	
	public enum Metric {SPRINT, CRUISE, IDLE, SETUP, REPAIR};
	public boolean trace = logger.isTraceEnabled();
	
	private Map<Metric,Map<Item,Double>> MetricToItemToFraction;
	private Clock clock;
	
	public TimeFractionsMetrics(Sim sim){
		MetricToItemToFraction = new HashMap<Metric,Map<Item,Double>>(Metric.values().length);
		final Machine machine = sim.getMachine();
		clock = sim.getClock();
		
		for (Metric metric : Metric.values()){
			MetricToItemToFraction.put(metric, new HashMap<Item,Double>(machine.getNumItems()));
			
			for (Item item : machine){
				MetricToItemToFraction.get(metric).put(item, 0.0);
			}			
		}
		
		Event.addBeforeEventListener(new BeforeEventListener(){
			@Override
			public void execute(Event event, Sim sim){
				double deltaTime = event.getTime() - sim.getTime();
				if (trace) {
					logger.trace("Updating time fractions with delta time " + deltaTime + " current time " +
							sim.getTime());
					}
								
				// Update the items' surpluses
				for (Item item : machine) {
					// Execute if the machine has this setup
					if (machine.getSetup().equals(item)) {
						// Machine up
						if (machine.getFailureState() == FailureState.UP) {
							increment(Metric.valueOf(machine.getOperationalState()+""),item,deltaTime);
						} else {
							increment(TimeFractionsMetrics.Metric.REPAIR,item, deltaTime);
						}
					}
				}
			}
		},sim);	
	}
	
	public void increment(Metric theMetric, Item theItem, Double theIncrement){
		if (clock.isTimeToRecordData()){
			double oldValue = MetricToItemToFraction.get(theMetric).get(theItem);
			MetricToItemToFraction.get(theMetric).put(theItem, oldValue + theIncrement);
		}
	}

	public Map<Metric, Map<Item, Double>> getMetricToItemToFraction() {
		return MetricToItemToFraction;
	}
		
}
