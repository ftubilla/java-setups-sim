package system.scheduler;

import sim.Sim;
import system.Machine.*;
import discreteEvent.*;

public class RoundRobin implements IScheduler {

	@Override
	public void updateControl(Sim sim) {
		
		if (sim.getMachine().getFailureState() == FailureState.UP){
		
			if(sim.getMachine().getOperationalState() != OperationalState.SETUP){
				if( sim.getMachine().getSetup().onOrAboveTarget()){
					//TODO Make this better
					if (sim.getMachine().getSetup().getId()==0){
						sim.getProductionSchedule().addEvent(new Changeover(sim.getTime(),sim.getMachine().getItemMap().get(1)));
					} else {
						sim.getProductionSchedule().addEvent(new Changeover(sim.getTime(),sim.getMachine().getItemMap().get(0)));
					}
				} else {
					double workRemaining = sim.getMachine().getSetup().workToTarget();
					sim.getMachine().setSprint();
					sim.getProductionSchedule().addEvent(new ControlEvent(sim.getTime() + workRemaining));
				}	
			} 
			else if (sim.getMachine().getOperationalState() == OperationalState.SETUP){
				sim.getMachine().setSprint();
				sim.getProductionSchedule().addEvent(new ControlEvent(sim.getTime()));
			}
			
		}
		
		return;
	}
}
