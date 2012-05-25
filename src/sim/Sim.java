package sim;

import java.util.*;
import discreteEvent.Event;
import sim.Params;

public class Sim {

	private Params params;
	private Queue<Event> schedule;
	private int time;

	public Sim(){
		this.schedule = new PriorityQueue<Event>();
		this.time = 0;
	}
	
	
	public Params getParams() {
		return params;
	}

	public void setParams(Params params) {
		this.params = params;
	}
	
	public void addEvent(Event e){
		schedule.add(e);
	}
	
	public void nextEvent(){
		(schedule.poll()).handle(this);
	}
	
	public void setTime(int newTime){
		this.time = newTime;
	}
	
	public int getTime(){
		return this.time;
	}
	
	public int getNumberOfEventsLeft(){
		return schedule.size();
	}
	
}
