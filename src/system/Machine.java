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
 * Has a FailureState and am OperationalState.
 * @author ftubilla
 *
 */
public class Machine implements Iterable<Item> {
	
	private static Logger logger = Logger.getLogger(Machine.class);
	public static enum FailureState {UP, DOWN};
	public static enum OperationalState {SPRINT, CRUISE, SETUP, IDLE};
	
	private Item setup;
	private Map<Integer,Item> itemMap;
	private List<Item> items;
	private OperationalState operationalState;
	private FailureState failureState;
	private MasterScheduler masterScheduler;
	private IProductionProcess productionProcess;
	
	
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

	public OperationalState getOperationalState() {
		return operationalState;
	}

	public FailureState getFailureState() {
		return failureState;
	}
	
	public void breakDown() {
		logger.debug("Setting the machine to FailureState DOWN");
		this.failureState = FailureState.DOWN;
		interruptProduction();
	}
	
	public void repair(){
		logger.debug("Setting the machine to FailureState UP");
		this.failureState = FailureState.UP;
		resumeProduction();
	}
	
	public void changeSetup(Item newSetup){
		assert operationalState != OperationalState.SETUP : "The machine is already changing setups";
		assert failureState != FailureState.DOWN : "The machine cannot change setups while it's down";
		logger.debug("Changing setup of the machine from " + setup + " to " + newSetup);
		this.setup = newSetup;
		this.operationalState = OperationalState.SETUP;
	}
	
	public void setIdle(){
		assert operationalState != OperationalState.IDLE : "The machine is already idle";
		logger.debug("The machine is set to IDLE");
		this.operationalState = OperationalState.IDLE;
		interruptProduction();
	}
	
	public void setCruise(){
		assert operationalState != OperationalState.CRUISE : "The machine is already cruising";
		assert failureState != FailureState.DOWN : "The machine cannot cruise if it's down";
		logger.debug("The machine is set to CRUISE");
		this.operationalState = OperationalState.CRUISE;
		//Note that cruising is in fact idling in a discrete production model and in a
		//continuous material model this "interruption" has no effect
		interruptProduction();
	}
	
	public void setSprint(){
		assert operationalState != OperationalState.SPRINT : "The machine is already sprinting";
		assert failureState != FailureState.DOWN : "The machine cannot sprint if it's down";
		logger.debug("The machine is set to SPRINT");
		this.operationalState = OperationalState.SPRINT;
		resumeProduction();
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
	
	private void resumeProduction(){
		if (masterScheduler.getSchedule(ScheduleType.PRODUCTION).eventsComplete()){
			//Get new production event
			masterScheduler.addEvent(productionProcess.getNextProductionDeparture(setup, Sim.time()));
		} else {
			//Get current production event
			masterScheduler.releaseAndDelayEvents();
		}
	}
	
	private void interruptProduction(){
		masterScheduler.holdDelayableEvents();
	}
	
}
