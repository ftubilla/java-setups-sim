package sim;

import java.util.*;
import discreteEvent.*;
import sim.Params;

public class Sim {

	private Params params;
	private Schedule failuresSchedule;
	private Schedule productionSchedule;
	private int time;

	public Sim(){
		this.time = 0;
		this.failuresSchedule = new Schedule();
		this.productionSchedule = new Schedule();
	}
	
	
	public Params getParams() {
		return params;
	}

	public void setParams(Params params) {
		this.params = params;
	}
	
	
	public void setTime(int newTime){
		this.time = newTime;
	}
	
	public int getTime(){
		return this.time;
	}


	public Schedule getFailuresSchedule() {
		return failuresSchedule;
	}


	public Schedule getProductionSchedule() {
		return productionSchedule;
	}
	
	public boolean eventsComplete(){
		if (productionSchedule.eventsComplete() && failuresSchedule.eventsComplete()){
			return true;
		}
		else{
			return false;
		}
	}
	
	public Event getNextEvent(){
		if (eventsComplete()){
			return null;
		}
		if (productionSchedule.eventsComplete()){
			return failuresSchedule.getNextEvent();
		}
		if (failuresSchedule.eventsComplete()){
			return productionSchedule.getNextEvent();
		}
		if (productionSchedule.nextEventTime() <= failuresSchedule.nextEventTime()){
			return productionSchedule.getNextEvent();
		} else {
			return failuresSchedule.getNextEvent();
		}
		
	}
	
}
