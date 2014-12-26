package metrics;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import sim.Clock;
import sim.Sim;
import system.Item;
import system.Machine;
import system.Machine.FailureState;
import discreteEvent.Event;
import discreteEvent.EventListener;


public class TimeFractionsMetrics {

	private static Logger logger = Logger.getLogger(TimeFractionsMetrics.class);
	
	public enum Metric {SPRINT, CRUISE, IDLE, SETUP, REPAIR, SETUP_FREQ};
	public boolean trace = logger.isTraceEnabled();
	
	private Map<Metric,Map<Item,Double>> metricToItemToFraction;
	private Clock clock;
	
	public TimeFractionsMetrics(Sim sim){
		metricToItemToFraction = new HashMap<Metric,Map<Item,Double>>(Metric.values().length);
		final Machine machine = sim.getMachine();
		clock = sim.getClock();
		
		for (Metric metric : Metric.values()){
			metricToItemToFraction.put(metric, new HashMap<Item,Double>(machine.getNumItems()));
			
			for (Item item : machine){
				metricToItemToFraction.get(metric).put(item, 0.0);
			}			
		}
		
		sim.getListenersCoordinator().addBeforeEventListener(new EventListener(){
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
							increment(TimeFractionsMetrics.Metric.REPAIR, item, deltaTime);
						}
					}
				}
			}
		});	
	}
	
	public void increment(Metric theMetric, Item theItem, Double theIncrement){
		if (clock.isTimeToRecordData()){
			double oldValue = metricToItemToFraction.get(theMetric).get(theItem);
			metricToItemToFraction.get(theMetric).put(theItem, oldValue + theIncrement);
			
			//Count the setup change if appropriate
			if (theMetric == Metric.SETUP) {
				//Note that if the machine is in SETUP state, theItem will equal the next setup
				double oldCount = metricToItemToFraction.get(Metric.SETUP_FREQ).get(theItem);
				metricToItemToFraction.get(Metric.SETUP_FREQ).put(theItem, oldCount + 1);
			}			
		}
	}

	public Double getFraction(Metric metric, Item item) {
		return metricToItemToFraction.get(metric).get(item) / clock.getMetricsRecordingTime(); 
	}
	
		
}
