package system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import processes.production.IProductionProcess;
import sim.Params;
import sim.Sim;
import discreteEvent.MasterScheduler;
import discreteEvent.ScheduleType;

/**
 * The main entity used in the sim. Holds the reference to the items that can be produced.
 * Has a FailureState and an OperationalState.
 * 
 * @author ftubilla
 *
 */
public class Machine implements Iterable<Item> {
	
	private static Logger logger = Logger.getLogger(Machine.class);
	
	public boolean trace = logger.isTraceEnabled();
	
	public static enum FailureState {UP, DOWN};
	public static enum OperationalState {SPRINT, CRUISE, SETUP, IDLE};
	
	private Item setup;
	private Map<Integer,Item> itemMap;
	private List<Item> items;
	private OperationalState operationalState;
	private FailureState failureState;
	private MasterScheduler masterScheduler;
	private IProductionProcess productionProcess;
	private double changingOverUntil;
	
	
	public Machine(Params params, MasterScheduler masterScheduler){
		
		int numItems = params.getNumItems();
		itemMap = new HashMap<Integer,Item>(numItems);
		items = new ArrayList<Item>(numItems);
		//Create items and itemSet
		for (int id = 0; id < params.getNumItems(); id++){
			Item item = new Item(id, params);
			items.add(item);
			itemMap.put(id, item);
		}
		logger.debug("Creating a machine with " + itemMap.size() + " items");
		
		//Set the initial setup
		logger.info("Machine has initial setup " + params.getInitialSetup());
		setup = itemMap.get(params.getInitialSetup());
		
		//Set the machine state
		failureState = FailureState.UP;
		operationalState = OperationalState.IDLE;
		
		this.masterScheduler = masterScheduler;
	}
	
	public Item getSetup() {
		return setup;
	}

	/**
	 * Returns the operational state of the machine. That is, if the machine is
	 * cruising, sprinting, idling or changing setups. These states are only
	 * valid when the machine is <em>operational</em> or up.
	 * @return operational state
	 */
	public OperationalState getOperationalState() {
		return operationalState;
	}

	/**
	 * Returns the failure (UP or DOWN) state of the machine.
	 * @return failureState
	 */
	public FailureState getFailureState() {
		return failureState;
	}
	
	public void breakDown() {
		assert isSetupComplete() : "The machine cannot fail while it is changing setups!";
		logger.debug("Setting the machine to FailureState DOWN");
		this.failureState = FailureState.DOWN;
		interruptProduction();
	}
	
	public void repair(){
		logger.debug("Setting the machine to FailureState UP");
		this.failureState = FailureState.UP;
		resumeProduction();
	}
	
	public void startChangeover(Item newSetup){
		logger.debug("Starting setup change from " + setup + " to " + newSetup);
		assert operationalState != OperationalState.SETUP : "The machine is already changing setups";	
		assert failureState != FailureState.DOWN : "The machine cannot change setups while it's down";
		changingOverUntil = newSetup.getSetupTime() + Sim.time();
		setup = newSetup;
		operationalState = OperationalState.SETUP;
	}
	
	public boolean isSetupComplete() {
		if(this.operationalState == OperationalState.SETUP) {
			if (trace){logger.trace("It is currently " + Sim.time() + " and the machine will be changing over until "
					+ changingOverUntil);}
			return Sim.time() >= changingOverUntil;
		} else {
			return true;
		}
	}
		
	public void setIdle(){
		assert isSetupComplete() : "Cannot change state of the machine until the setup is complete";
		if (!isIdling()){
			logger.debug("The machine is set to IDLE");
			this.operationalState = OperationalState.IDLE;
			interruptProduction();
		}
	}
	
	public void setCruise(){
		assert isSetupComplete() : "Cannot change state of the machine until the setup is complete";
		assert !productionProcess.isDiscrete() : "Cruising is currently NOT implemented on discrete production processes";
		if (!isCruising()){
			assert failureState != FailureState.DOWN : "The machine cannot cruise if it's down";
			logger.debug("The machine is set to CRUISE");
			this.operationalState = OperationalState.CRUISE;
		}
	}
	
	public void setSprint(){
		assert isSetupComplete() : "Cannot change state of the machine until the setup is complete";
		if (!isSprinting()) {
			assert failureState != FailureState.DOWN : "The machine cannot sprint if it's down";
			logger.debug("The machine is set to SPRINT");
			this.operationalState = OperationalState.SPRINT;
			resumeProduction();
		}
	}
	
	public Item getItemById(int id){
		return items.get(id);
	}

	@Override
	public Iterator<Item> iterator() {
		return items.iterator();
	}

	public int getNumItems() {
		return items.size();
	}

	public void setProductionProcess(IProductionProcess productionProcess){
		this.productionProcess = productionProcess;
	}
	
	public MachineSnapshot getSnapshot(){
		return new MachineSnapshot(this);
	}
	
	public boolean isIdling(){return operationalState==OperationalState.IDLE;}
	public boolean isCruising(){return operationalState==OperationalState.CRUISE;}
	public boolean isSprinting(){return operationalState==OperationalState.SPRINT;}
	public boolean isDown() {return failureState == FailureState.DOWN;}
	public boolean isUp() {return failureState == FailureState.UP;}
	public boolean isChangingSetups() {return operationalState == OperationalState.SETUP;}
	
	private void resumeProduction(){
		logger.trace("Resuming production of machine");
		masterScheduler.releaseAndDelayEvents();
		if (masterScheduler.getSchedule(ScheduleType.PRODUCTION).eventsComplete()){
			//Get new production event
			masterScheduler.addEvent(productionProcess.getNextProductionDeparture(setup, Sim.time()));
		}
	}
	
	private void interruptProduction(){
		logger.trace("Interrupting production");
		masterScheduler.holdDelayableEvents();
	}

	public double getNextSetupCompleteTime() {
		return changingOverUntil;
	}

	
}
