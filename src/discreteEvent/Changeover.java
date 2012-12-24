package discreteEvent;

import org.apache.log4j.Logger;

import sim.Sim;
import system.*;

public class Changeover extends Event {

	private static int idCount = 0;
	private static Logger logger = Logger.getLogger(Changeover.class);

	private Item changeTo;

	// Note that this marks the start of the changeover. At the end of the
	// changeover we will call the policy.
	public Changeover(double time, Item changeTo) {
		super(time);
		Changeover.idCount++;
		this.changeTo = changeTo;
	}

	@Override
	public void handle(Sim sim) {
		super.handle(sim);

		// Set the changeover, delay upcoming failures/repairs, and call the
		// policy when done
		double changeoverTime = sim.getParams().getSetupTimes()
				.get(changeTo.getId());
		logger.debug("Changing the machine's setup to Item " + changeTo.getId()
				+ " with a changeover time " + changeoverTime);
		sim.getMachine().changeSetup(changeTo);
		logger.debug("Delaying all failure events by " + changeoverTime);
		sim.getFailuresSchedule().delayEvents(changeoverTime);
		sim.getProductionSchedule().addEvent(
				new ControlEvent(sim.getTime() + changeoverTime));

	}

	public static int getCount() {
		return Changeover.idCount;
	}

}
