package policies;

import java.util.Iterator;

import org.apache.log4j.Logger;

import sim.Sim;
import system.Item;
import system.Machine;
import system.Machine.FailureState;
import system.Machine.OperationalState;
import discreteEvent.Changeover;
import discreteEvent.ControlEvent;

public class RoundRobinPolicy implements IPolicy {

	private static Logger logger = Logger.getLogger(RoundRobinPolicy.class);

	private Iterator<Item> itemsIterator;
	private Machine machine;

	@Override
	public void updateControl(Sim sim) {

		if (sim.getMachine().getFailureState() == FailureState.DOWN) {
			// Flush the production schedule
			sim.getProductionSchedule().dumpEvents();
		}

		if (sim.getMachine().getFailureState() == FailureState.UP) {

			if (sim.getMachine().getOperationalState() != OperationalState.SETUP) {
				if (sim.getMachine().getSetup().onOrAboveTarget()) {

					logger.debug("Scheduling a changeover to a new item");
					sim.getProductionSchedule().addEvent(
							new Changeover(sim.getTime(), nextItem()));

				} else {
					double workRemaining = sim.getMachine().getSetup()
							.workToTarget();
					logger.debug("Sprint until the next control event after "
							+ workRemaining);
					sim.getMachine().setSprint();
					sim.getProductionSchedule().addEvent(
							new ControlEvent(sim.getTime() + workRemaining));
				}
			} else if (sim.getMachine().getOperationalState() == OperationalState.SETUP) {
				logger.debug("The machine is ready to start producing new item."
						+ " Next control event will determine how much work to do");
				sim.getMachine().setSprint();
				sim.getProductionSchedule().addEvent(
						new ControlEvent(sim.getTime()));
			}

		}

		return;
	}

	@Override
	public void setup(Sim sim) {

		machine = sim.getMachine();
		itemsIterator = machine.iterator();
		// Set the iterator to point towards the initial setup
		while (itemsIterator.hasNext()) {
			if (sim.getParams().getInitialSetup() == itemsIterator.next()
					.getId()) {
				logger.debug("Setting items iterator to start at item "
						+ sim.getParams().getInitialSetup());
				break;
			}
		}
	}

	private Item nextItem() {
		if (!itemsIterator.hasNext()) {
			itemsIterator = machine.iterator();
		}
		return itemsIterator.next();
	}

}
