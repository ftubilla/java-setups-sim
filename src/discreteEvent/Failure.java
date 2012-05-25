package discreteEvent;

import sim.Sim;

public class Failure extends Event {

	private static int idCount = 0;
	
	public Failure(int time){
		super(time);
		Failure.idCount++;
	}
	
	@Override
	public void handle(Sim sim){
		super.handle(sim);
		int repairTime = 5;
		sim.getFailuresSchedule().addEvent(new Repair(sim.getTime() + repairTime));
		sim.getProductionSchedule().delayEvents(repairTime);
	}
	
	
	public static int getCount(){
		return Failure.idCount;
	}
}
