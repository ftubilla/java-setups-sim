package policies;

import discreteEvent.Changeover;
import discreteEvent.ControlEvent;
import sim.Sim;
import system.Machine.*;

public class UnstableCCPolicyN3 implements IPolicy {

	private final double K = 3.1;
	private final double Delta = 3;
	
	@Override
	public void updateControl(Sim sim) {
		//Strictly speaking, this example policy is intended for reliable systems
		if (sim.getMachine().getFailureState() == FailureState.DOWN){
			sim.getMasterScheduler().dumpEvents();
		}
		else{
			//Machine is UP
			
			if(sim.getMachine().getOperationalState() != OperationalState.SETUP){
				if( sim.getMachine().getSetup().onOrAboveTarget()){
					
					// Find the next changeover
					double y1 = sim.getMachine().getItemById(1).getSurplusDeviation();
					double y2 = sim.getMachine().getItemById(2).getSurplusDeviation();
					int setup = sim.getMachine().getSetup().getId();
					int changeTo = -1;
					
					
					//First the "slice" setup zones
					if (setup != 2){
						double n2 = Math.ceil(Math.log10(y2)/Math.log10(K));
						if(n2 % 2 == 1 & y2 >= Math.pow(K, n2)-Delta & n2 > 2 ){					
							System.out.println("Made it 1");
							changeTo = 2;
						}
					}
					else if (setup != 1){
						double n1 = Math.ceil(Math.log10(y1)/Math.log10(K));
						if (n1 % 2 == 0 & y1 >= Math.pow(K, n1)-Delta & n1 > 3){
							System.out.println("Made it 2");
							changeTo = 1;
						}
					}
					
					if (changeTo == -1){
					// The regular setup zones
						if (setup == 0){
							//If finished 0, go to the one with the *smallest* deviation!
							if (y1>y2) {
								changeTo = 2;
							}
							else {
								changeTo = 1;
							}
						} else {
							//If finished 1 or 2, switch to 0
							changeTo = 0;
						}
					}

				
					//System.out.println("Changing to " + changeTo);
					// Add the changeover event
					sim.getMasterScheduler().addEvent(new Changeover(sim.getTime(),
							sim.getMachine().getItemById(changeTo)));
					
				} else {
					// Continue Producing
					double workRemaining = sim.getMachine().getSetup().workToTarget();
					sim.getMachine().setSprint();
					sim.getMasterScheduler().addEvent(new ControlEvent(sim.getTime() + workRemaining));
				}	
			} 
			else if (sim.getMachine().getOperationalState() == OperationalState.SETUP){
				// Start producing
				sim.getMachine().setSprint();
				sim.getMasterScheduler().addEvent(new ControlEvent(sim.getTime()));
			}
			
		}
		
		

	}

	@Override
	public void setup(Sim sim) {
		// TODO Auto-generated method stub
		assert sim.getParams().getNumItems() == 3;		
	}

}
