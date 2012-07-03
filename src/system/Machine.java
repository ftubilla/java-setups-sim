package system;

import java.util.*;
import sim.*;

public class Machine {
	
	private Item setup;
	private Map<Integer,Item> itemMap;
	private OperationalState operationalState;
	private FailureState failureState;
	
	
	public enum FailureState {UP, DOWN};
	public enum OperationalState {SPRINT, CRUISE, SETUP, IDLE};
	
	public Machine(Params params){
		
		int numItems = params.getNumItems();
		itemMap = new HashMap<Integer,Item>(numItems);
		//Create items and itemSet
		for (int id = 0; id < params.getNumItems(); id++){
			itemMap.put(id, new Item(id, params));
		}
		
		//Set the initial setup
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
		this.failureState = FailureState.DOWN;
	}
	public void repair(){
		this.failureState = FailureState.UP;
	}
	public void changeSetup(Item item){
		this.setup = item;
		this.operationalState = OperationalState.SETUP;
	}
	public void setIdle(){
		this.operationalState = OperationalState.IDLE;
	}
	public void setCruise(){
		this.operationalState = OperationalState.CRUISE;
	}
	public void setSprint(){
		this.operationalState = OperationalState.SPRINT;
	}
	
	public Map<Integer,Item> getItemMap(){
		return itemMap;
	}
	
	public Collection<Item> getItems(){
		return itemMap.values();
	}
		
	
}
