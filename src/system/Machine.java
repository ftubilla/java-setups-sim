package system;

import java.util.*;

import org.apache.log4j.Logger;

import sim.*;

/**
 * The main entity used in the sim. Holds the reference to the items that can be produced.
 * Has a FailureState and am OperationalState.
 * @author ftubilla
 *
 */
public class Machine implements Iterable<Item> {
	
	private static Logger logger = Logger.getLogger(Machine.class);
	
	private Item setup;
	private Map<Integer,Item> itemMap;
	private List<Item> items;
	private OperationalState operationalState;
	private FailureState failureState;
	
	
	public static enum FailureState {UP, DOWN};
	public static enum OperationalState {SPRINT, CRUISE, SETUP, IDLE};
	
	public Machine(Params params){
		
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
	}
	
	public void repair(){
		logger.debug("Setting the machine to FailureState UP");
		this.failureState = FailureState.UP;
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
		logger.debug("The machine is idling");
		this.operationalState = OperationalState.IDLE;
	}
	
	public void setCruise(){
		assert operationalState != OperationalState.CRUISE : "The machine is already cruising";
		assert failureState != FailureState.DOWN : "The machine cannot cruise if it's down";
		logger.debug("The machine is cruising");
		this.operationalState = OperationalState.CRUISE;
	}
	
	public void setSprint(){
		assert operationalState != OperationalState.SPRINT : "The machine is already sprinting";
		assert failureState != FailureState.DOWN : "The machine cannot sprint if it's down";
		logger.debug("The machine is sprinting");
		this.operationalState = OperationalState.SPRINT;
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
	
}
