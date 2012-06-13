package discreteEvent;

import sim.Sim;

public class ControlEvent extends Event {

	private static int idCount = 0;
	
	public ControlEvent(double time){
		super(time);
		ControlEvent.idCount++;
	}
	
	@Override
	public void handle(Sim sim){
		super.handle(sim);
		
		sim.getTheScheduler().updateControl(sim);
		
	}
	public static int getCount(){
		return ControlEvent.idCount;
	}
	
}


