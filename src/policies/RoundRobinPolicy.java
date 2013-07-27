package policies;

import java.util.Iterator;

import org.apache.log4j.Logger;

import sim.Sim;
import system.Item;

public class RoundRobinPolicy extends AbstractPolicy {

	private static Logger logger = Logger.getLogger(RoundRobinPolicy.class);

	private Iterator<Item> itemsIterator;

	
	@Override
	public void machineReady(){
		
		if (machine.getSetup().onOrAboveTarget()) {			
			startChangeover(nextItem());		
		} else {
			double workRemaining = machine.getSetup().minPossibleWorkToTarget(demandProcess);
			machine.setSprint();
			updateControlAfter(workRemaining);
		}
		
	}

	
	@Override
	public void setUp(Sim sim) {
		super.setUp(sim);
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
