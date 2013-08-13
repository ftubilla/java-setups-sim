package policies;

import org.apache.log4j.Logger;

import processes.demand.IDemandProcess;
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
	
	protected Machine machine;
	protected IDemandProcess demandProcess;
	protected PolicyParams policyParams;
	
	
	public void updateControl(Sim sim){

		if (machine.isDown()) {			
			logger.trace("Machine is down.");
			machineDown();			
		}
		
		if (machine.isUp()) {

			if (!machine.isChangingSetups()) {	
				
				if (isTimeToChangeOver()){
					logger.trace("The machine is ready to change setups");
					startChangeover(nextItem());
				} else {					
					logger.trace("The machine is in the middle of a production run");
					double nextUpdate = doUntilNextUpdate();
					updateControlAfter(nextUpdate);
				}
				
			} else if (machine.isSetupComplete()) {
				
				logger.trace("The machine has finished its setup change." +
						" Next control event will determine how much work to do");
				machine.setSprint();
				updateControlAfter(0);
				
			} else {
				double newControlTime = machine.getNextSetupCompleteTime() - Sim.time();
				logger.debug("Nothing to do. Setups are non-preemptive. Scheduling a new control for time " + newControlTime);
				updateControlAfter(newControlTime);
			}
		}								
	}
	
	@Override
	public void setUpPolicy(Sim sim){
		this.sim = sim;
		this.machine = sim.getMachine();
		this.demandProcess = sim.getDemandProcess();
		this.policyParams = sim.getParams().getPolicyParams();
	}
	
	/**
	 * Override with whatever logic the policy implements when the machine is down.
	 * @param sim
	 */
	protected void machineDown(){
		// Flush the control schedule
		sim.getMasterScheduler().dumpEvents();
	}
	
	/**
	 * The machine is ready to produce, i.e., it is not under repair, and no changeover is in progress or due at this time.
	 * Override with any commands you want to execute during the run, and return the time when a new control update should 
	 * occur.
	 * 
	 * @return double Time for next control update
	 */
	protected abstract double doUntilNextUpdate();
	
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
		lastChangeoverTime = Sim.time();
	}
	
	/**
	 * A helper method to simplify the policy class. Adds a control update event
	 * occurring updatePeriod time units after the current time.
	 * @param updatePeriod
	 * 
	 */
	protected void updateControlAfter(double updatePeriod){
		if (trace) {logger.trace("Scheduling the next control event to occur after " +
					updatePeriod + " time units");}
		sim.getMasterScheduler().addEvent(new ControlEvent(sim.getTime() + updatePeriod));
	}
		
}

