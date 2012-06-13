package discreteEvent;

import sim.Sim;

public class Repair extends Event {
	
	private static int idCount = 0;
	
	public Repair(double time){
		super(time);
		Repair.idCount++;
	}
	
	@Override
	public void handle(Sim sim){
		super.handle(sim);

		//Generate the next failure
		sim.getFailuresSchedule().addEvent(new Failure(sim.getTime() + sim.getTheFailuresGenerator().nextTimeInterval()));
		sim.getMachine().repair();
		sim.getTheScheduler().updateControl(sim);
	}
	
	
	public static int getCount(){
		return Repair.idCount;
	}
	
}
