package metrics;

import java.util.*;
import system.*;
import sim.*;


public class TimeFractionsMetrics {

	public enum Metric {SPRINT, CRUISE, IDLE, SETUP, REPAIR};
	
	private Map<Metric,Map<Item,Double>> MetricToItemToFraction;
	
	
	public TimeFractionsMetrics(Machine machine){
		MetricToItemToFraction = new HashMap<Metric,Map<Item,Double>>(Metric.values().length);
		
		for (Metric metric : Metric.values()){
			MetricToItemToFraction.put(metric, new HashMap<Item,Double>(machine.getNumItems()));
			
			for (Item item : machine){
				MetricToItemToFraction.get(metric).put(item, 0.0);
			}
			
		}
		
	}

	
	public void increment(Metric theMetric, Item theItem, Double theIncrement){
		if (Sim.TIME_TO_START_RECORDING){
			double oldValue = MetricToItemToFraction.get(theMetric).get(theItem);
			MetricToItemToFraction.get(theMetric).put(theItem, oldValue + theIncrement);
		}
	}


	public Map<Metric, Map<Item, Double>> getMetricToItemToFraction() {
		return MetricToItemToFraction;
	}
	
		
}
