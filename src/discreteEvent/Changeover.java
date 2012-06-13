package discreteEvent;

import sim.Sim;
import system.*;

public class Changeover extends Event {

	private static int idCount = 0;
	
	private Item changeTo;
	
	
	//Note that this marks the start of the changeover. At the end of the changeover we will call the policy.
	public Changeover(double time, Item changeTo){
		super(time);
		Changeover.idCount++;
		this.changeTo = changeTo;
	}
	
	@Override
	public void handle(Sim sim){
		super.handle(sim);
		
		//Set the changeover, delay upcoming failures/repairs, and call the policy when done
		double changeoverTime = sim.getParams().getSetupTimes().get(changeTo.getId());
		sim.getMachine().changeSetup(changeTo);
		sim.getFailuresSchedule().delayEvents(changeoverTime);
		sim.getProductionSchedule().addEvent(new ControlEvent(sim.getTime() + changeoverTime));
		
	}
	public static int getCount(){
		return Changeover.idCount;
	}
	
}

