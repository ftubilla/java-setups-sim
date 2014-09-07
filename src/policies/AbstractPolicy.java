package policies;

import org.apache.log4j.Logger;

import params.PolicyParams;

import sim.Clock;
import sim.Sim;
import system.Item;
import system.Machine;
import discreteEvent.Changeover;
import discreteEvent.ControlEvent;

public abstract class AbstractPolicy implements IPolicy {
	
	private static Logger logger = Logger.getLogger(AbstractPolicy.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();
	private boolean trace = logger.isTraceEnabled();

	private Sim sim;
	private double lastChangeoverTime = -1;
	
	protected Item currentSetup;
	protected Machine machine;
	protected boolean hasDiscreteMaterial;
	protected PolicyParams policyParams;
	protected Clock clock;
	
	
	public void updateControl(Sim sim){

		currentSetup = machine.getSetup();
		clock = sim.getClock();
		
		if (machine.isDown()) {			
			logger.trace("Machine is down.");
			onFailure();			
		}
		
		if (machine.isUp()) {

			if (!machine.isChangingSetups()) {	
				
				if (isTimeToChangeOver()){
					logger.trace("The machine is ready to change setups");
					startChangeover(nextItem());
				} else {					
					logger.trace("The machine is in the middle of a production run");
					sim.getMasterScheduler().addEvent(onReady());
				}
				
			} else if (machine.isSetupComplete()) {				
				logger.trace("The machine has finished its setup change." +
						" Next control event will determine how much work to do");
				machine.setSprint();
				sim.getMasterScheduler().addEvent(new ControlEvent(sim.getTime()));
				
			} else {
				logger.debug("Nothing to do. Setups are non-preemptive.");
				sim.getMasterScheduler().addEvent(new ControlEvent(machine.getNextSetupCompleteTime()));
			}
		}								
	}
	
	@Override
	public void setUpPolicy(Sim sim){
		this.sim = sim;
		this.machine = sim.getMachine();
		this.hasDiscreteMaterial = sim.hasDiscreteMaterial();
		this.policyParams = sim.getParams().getPolicyParams();
	}
	
	/**
	 * Override with whatever logic the policy implements when the machine is down.
	 * @param sim
	 */
	protected void onFailure(){
		// Flush the control schedule
		sim.getMasterScheduler().dumpEvents();
	}
	
	/**
	 * The machine is ready to produce, i.e., it is not under repair, and no changeover is in progress or due at this time.
	 * Override with any commands you want to execute during the run, and return the time when a new control update should 
	 * occur.
	 * 
	 * @return ControlEvent An event for when the next update should occur
	 */
	protected abstract ControlEvent onReady();
	
	/**
	 * Returns true if the machine should start changing over to some other item, given by nextItem.
	 * @return boolean
	 */
	protected abstract boolean isTimeToChangeOver();
	
	/**
	 * Implements the decision rule for selecting the next item if a changeover is valid.
	 * If the state does not allow a changeover, the returned item is undefined (could be null).
	 * @return
	 */
	protected abstract Item nextItem();
		
	/**
	 * A helper method to simplify the policy class. Adds a changeover event to the
	 * given item to start now.
	 * @param item
	 * 
	 */
	protected void startChangeover(Item item){
		if (trace) {logger.trace("Scheduling a changeover to a new item. Last changeover was at " + 
				lastChangeoverTime);}
		assert item != machine.getSetup() : "Cannot changeover to the current setup again!";
		sim.getMasterScheduler().addEvent(new Changeover(sim.getTime(), item));
		lastChangeoverTime = clock.getTime();
	}

}


