package discreteEvent;

import org.apache.log4j.Logger;

import sim.Sim;

public class ControlEvent extends Event {

	private static Logger logger = Logger.getLogger(ControlEvent.class);
	private static int idCount = 0;
	
	public ControlEvent(double time){
		super(time);
		ControlEvent.idCount++;
	}
	
	@Override
	public void handle(Sim sim){
		super.handle(sim);
		logger.debug("Processing control event at time " + sim.getTime());
		sim.getPolicy().updateControl(sim);
		
	}
	public static int getCount(){
		return ControlEvent.idCount;
	}
	
}


