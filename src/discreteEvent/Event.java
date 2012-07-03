/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * © 2012 Fernando Tubilla. All rights reserved.
 */

package discreteEvent;

import metrics.TimeFractionsMetrics;
import sim.Sim;
import system.*;
import system.Machine.FailureState;

public class Event implements Comparable<Event> {

	private double time;
	private int id;
	private static int idCount = 0;


	
	public Event(double time){
		this.time = time;
		this.id = idCount;
		Event.idCount++;
	}
	
	public int compareTo(Event otherEvent){
		return (this.time < otherEvent.time ? -1 : (this.time == otherEvent.time ? 0 : 1)); 				
	}
	
	
	public void handle(Sim sim){
			
		double deltaTime = time - sim.getTime();
		
		//Check if it's time to start recording data 
		if (sim.getTime() >= sim.getParams().getMetricsStartTime()){
			if (Sim.TIME_TO_START_RECORDING == false){
				Sim.METRICS_INITIAL_TIME = sim.getTime();
			}
			Sim.TIME_TO_START_RECORDING = true;
		}
		
	
		//Update the items' surpluses
		for (Item item : sim.getMachine().getItems()){
			
			//Cumulative demand always goes up
			item.setCumulativeDemand(item.getCumulativeDemand() + item.getDemandRate()*deltaTime);
			
			//Execute if the machine has this setup
			if (sim.getMachine().getSetup().equals(item)){
				
					//Machine up
				if (sim.getMachine().getFailureState()==FailureState.UP){
				
					switch(sim.getMachine().getOperationalState()){
						case SPRINT:
							item.setCumulativeProduction(item.getCumulativeProduction() + item.getProductionRate()*deltaTime);
							//Update Metrics
							sim.getMetrics().getTimeFractions().increment(TimeFractionsMetrics.Metric.SPRINT, item, deltaTime);
							break;
							
						case CRUISE:
							item.setCumulativeProduction(item.getCumulativeProduction() + item.getDemandRate()*deltaTime);
							//Update Metrics
							sim.getMetrics().getTimeFractions().increment(TimeFractionsMetrics.Metric.CRUISE, item, deltaTime);
							break;
							
						case IDLE:
							//Update Metrics
							sim.getMetrics().getTimeFractions().increment(TimeFractionsMetrics.Metric.IDLE, item, deltaTime);
							break;
							
						case SETUP:
							//Update Metrics
							sim.getMetrics().getTimeFractions().increment(TimeFractionsMetrics.Metric.SETUP, item, deltaTime);
					}
				
					
				} else {
					//Machine down
					sim.getMetrics().getTimeFractions().increment(TimeFractionsMetrics.Metric.REPAIR, item, deltaTime);
				}
			}
		}
		
		
		if (Sim.DEBUG){
			System.out.println(this.getClass().getSimpleName() + " has occurred at time " + time);
		}
		
		//Call other recorders
		sim.getRecorders().getFailureEventsRecorder().record(sim);
		
		//Advance time
		sim.setTime(time);
		sim.setLatestEvent(this);
	}
		
	
	
	public double getTime() {
		return time;
	}

	public void updateTime(double time) {
		this.time = time;
	}

	public int getId() {
		return id;
	}
	
	public static int getCount(){
		return Event.idCount;
	}
		
}
