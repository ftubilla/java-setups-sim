package discreteEvent;

import org.apache.log4j.Logger;

import processes.demand.IDemandProcess;
import processes.production.IProductionProcess;
import system.Item;

/**
 * An event for capturing the moment in which we reach a desired surplus level.
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
	
	public SurplusControlEvent(Item item, double targetSurplus, double currentTime,
			IProductionProcess productionProcess, IDemandProcess demandProcess) {
		//Estimate the time to hit and create the event
		super(computeTimeToHit(item,targetSurplus,productionProcess,demandProcess)+currentTime);
	}
	
	private static double computeTimeToHit(Item item, double targetSurplus,
			IProductionProcess productionProcess, IDemandProcess demandProcess) {
							
		assert item.isUnderProduction() : "Cannot create this event for an item that's not under production!";
		
		if (!productionProcess.isDiscrete() && !demandProcess.isDiscrete()) {
			double surplusDiff = targetSurplus - item.getSurplus();
			if (surplusDiff <= 0){
				return -surplusDiff/item.getDemandRate();
			} else{
				return surplusDiff/(item.getProductionRate()-item.getDemandRate());
			}
		} else {	
			//We cannot predict a priori when the target will be reached for discrete processes,
			//so set an event to occur "at infinity". We will nevertheless update the control
			//every time that a demand or production occurs.
			return Double.MAX_VALUE;
		}		
	}

}


