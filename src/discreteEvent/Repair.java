package discreteEvent;

import sim.Sim;

public class Repair extends Event {
	
	private static int idCount = 0;
	
	public Repair(int time){
		super(time);
		Repair.idCount++;
	}
	
	@Override
	public void handle(Sim sim){
		super.handle(sim);
		int failureTime = 5;
		sim.getFailuresSchedule().addEvent(new Failure(sim.getTime() + failureTime));
	}
	
	
	public static int getCount(){
		return Repair.idCount;
	}
	
}
