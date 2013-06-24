package discreteEvent;

import org.apache.log4j.Logger;

import sim.Sim;

public class Repair extends Event {
	
	private static Logger logger = Logger.getLogger(Repair.class);
	private static int idCount = 0;
	
	public Repair(double time){
		super(time);
		Repair.idCount++;
	}
	
	@Override
	public void mainHandle(Sim sim){
		
		//Generate the next failure
		double nextTimeToFailure = sim.getTheFailuresGenerator().nextTimeInterval();
		sim.getMasterScheduler().addEvent(new Failure(sim.getTime() + nextTimeToFailure));
		logger.debug("Finished repairing the machine. Next TTF is " + nextTimeToFailure);
		sim.getMachine().repair();
		sim.getPolicy().updateControl(sim);
	}
	
	
	public static int getCount(){
		return Repair.idCount;
	}
	
}
