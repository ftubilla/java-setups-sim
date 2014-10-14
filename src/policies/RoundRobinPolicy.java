package policies;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import sim.Sim;
import system.Item;
import discreteEvent.ControlEvent;
import discreteEvent.SurplusControlEvent;

public class RoundRobinPolicy extends AbstractPolicy {

	private Map<Item, Item> nextItemMap = new HashMap<Item, Item>();

	@Override
	protected boolean isTimeToChangeOver() {
		return machine.getSetup().onOrAboveTarget();
	}
	
	@Override
	protected ControlEvent onReady() {
		machine.setSprint();
		return new SurplusControlEvent(currentSetup,currentSetup.getSurplusTarget(),clock.getTime(),hasDiscreteMaterial);
	}
	
	@Override
	public void setUpPolicy(Sim sim) {
		super.setUpPolicy(sim);
		//Fill the transition map from item to the next.
		Item firstItem = null;
		Item prevItem = null;
		for (Item item : machine) {
			if (firstItem == null) {
				firstItem = item;
				prevItem = item;
			}
			nextItemMap.put(prevItem, item);
			prevItem = item;
		}
		nextItemMap.put(prevItem, firstItem);
	}

	@Override
	protected Item nextItem() {
		if (isTimeToChangeOver()){
			assert nextItemMap.containsKey(currentSetup) : "The next Item map should contain an entry for the current setup " + currentSetup;
			//Note that by using a map, the function is idempotent (i.e., repeated calls to the function with the same current setup give the same result)
			return nextItemMap.get(currentSetup);
		} else {
			return null;
		}
	}

	@Override
	public boolean isTargetBased() {
		return true;
	}


}
