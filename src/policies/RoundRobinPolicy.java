package policies;

import java.util.Iterator;

import org.apache.log4j.Logger;

import sim.Sim;
import system.Item;

public class RoundRobinPolicy extends AbstractPolicy {

	private static Logger logger = Logger.getLogger(RoundRobinPolicy.class);

	private Iterator<Item> itemsIterator;

	@Override
	protected boolean isTimeToChangeOver() {
		return machine.getSetup().onOrAboveTarget();
	}
	
	@Override
	protected double onReady() {
		machine.setSprint();
		return machine.getSetup().computeMinDeltaTimeToTarget(productionProcess, demandProcess);
	}
	
	@Override
	public void setUpPolicy(Sim sim) {
		super.setUpPolicy(sim);
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

	protected Item nextItem() {
		if (!itemsIterator.hasNext()) {
			itemsIterator = machine.iterator();
		}
		return itemsIterator.next();
	}

	@Override
	public boolean isTargetBased() {
		return true;
	}


}
