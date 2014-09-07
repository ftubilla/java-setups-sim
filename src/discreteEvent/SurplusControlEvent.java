package discreteEvent;

import org.apache.log4j.Logger;

import system.Item;

/**
 * An event for capturing the moment in which we reach a desired surplus level. For discrete processes,
 * this event should never trigger and it is not necessary (because production departures and demand
 * arrivals always trigger control events).
 * 
 * @author ftubilla
 *
 */
public class SurplusControlEvent extends ControlEvent {

	private static Logger logger = Logger.getLogger(SurplusControlEvent.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();
	@SuppressWarnings("unused")
	private boolean trace = logger.isTraceEnabled();
	
	public SurplusControlEvent(Item item, double targetSurplus, double currentTime, boolean hasDiscreteMaterial) {
		//Estimate the time to hit and create the event
		super(computeTimeToHit(item,targetSurplus,hasDiscreteMaterial)+currentTime);
	}
	
	private static double computeTimeToHit(Item item, double targetSurplus, boolean hasDiscreteMaterial){
		if(!hasDiscreteMaterial){
			assert item.isUnderProduction() : "Cannot create this event for an item that's not under production!";
			return item.getFluidTimeToSurplusLevel(targetSurplus);
		} else {
			//In this case, we only need to look at demand arrivals and production departures, which already trigger control events
			return Double.MAX_VALUE;
		}
	}
	
}


