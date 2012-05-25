package sim;

import discreteEvent.*;

public class SimRun {

	public static void run(Sim sim){
		
		//Create a few event
		Event e1 = new Event(10);
		Event e2 = new Event(1);
		Event e3 = new Event(30);
		
		sim.addEvent(e1);
		sim.addEvent(e2);
		sim.addEvent(e3);
		
		while(sim.getTime() < 100 && sim.getNumberOfEventsLeft() > 0){
			sim.nextEvent();
		}
		
	}
	
}
