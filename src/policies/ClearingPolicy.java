package policies;

import discreteEvent.ControlEvent;
import discreteEvent.SurplusControlEvent;

/**
 * A policy is in the clearing class if:
 * 	
 *  1) it never switches setups unless the current item's surplus is at ZU
 *  
 *  2) it always produces at maximum rate
 *  
 * @author ftubilla
 *
 */
public abstract class ClearingPolicy extends AbstractPolicy {

	/**
	 * Under a clearing policy, we only switch setups if the current item's
	 * surplus is at or above the target.
	 * 
	 */
	@Override
	protected boolean isTimeToChangeOver() {
		return machine.getSetup().onOrAboveTarget();
	}

	/**
	 * When the machine is ready, under a clearing policy we will sprint (produce at full capacity)
	 * until the current surplus target is reached.
	 */
	@Override
	protected ControlEvent onReady() {
		machine.setSprint();
		return new SurplusControlEvent(currentSetup, currentSetup.getSurplusTarget(), clock.getTime(), hasDiscreteMaterial);
	}

	/**
	 * All clearing policies are target based.
	 */
	@Override
	public boolean isTargetBased() {
		return true;
	}


}


