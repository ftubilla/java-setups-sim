package discreteEvent;

import org.apache.log4j.Logger;

import sim.Sim;

public class Failure extends Event {

	private static Logger logger = Logger.getLogger(Failure.class);
	private static int idCount = 0;

	public Failure(double time) {
		super(time);
		Failure.idCount++;
	}

	@Override
	public void handle(Sim sim) {

		super.handle(sim);
		// Repair machine and delay the production schedule
		double repairTime = sim.getTheRepairsGenerator().nextTimeInterval();
		logger.debug("Processing failure event. Machine will be repaired after "
				+ repairTime + " time units");
		sim.getFailuresSchedule().addEvent(
				new Repair(sim.getTime() + repairTime));
		sim.getMachine().breakDown();
		sim.getPolicy().updateControl(sim);
	}

	public static int getCount() {
		return Failure.idCount;
	}

}
