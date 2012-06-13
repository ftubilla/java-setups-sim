package discreteEvent;

import sim.Sim;

public class Failure extends Event {

	private static int idCount = 0;


	public Failure(double time){
		super(time);
		Failure.idCount++;
	}
	
	@Override
	public void handle(Sim sim){

		super.handle(sim);		
		//Repair machine and delay the production schedule
		double repairTime = sim.getTheRepairsGenerator().nextTimeInterval();
		sim.getFailuresSchedule().addEvent(new Repair(sim.getTime() + repairTime));
		sim.getMachine().breakDown();
		sim.getTheScheduler().updateControl(sim);
	}
	
	
	public static int getCount(){
		return Failure.idCount;
	}
	

}
