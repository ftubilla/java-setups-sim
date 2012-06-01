package system;

import java.util.*;
import sim.*;

public class Machine {
	
	private Item setup;
	private Set<Item> itemSet;
	private OperationalState operationalState;
	private FailureState failureState;
	
	
	public enum FailureState {UP, DOWN};
	public enum OperationalState {SPRINT, CRUISE, SETUP, IDLE};
	
	public Machine(Params params){
		
	}
	
	
	public Item getSetup() {
		return setup;
	}
	public void setSetup(Item setup) {
		this.setup = setup;
	}
	public OperationalState getOperationalState() {
		return operationalState;
	}
	public void setOperationalState(OperationalState operationalState) {
		this.operationalState = operationalState;
	}
	public FailureState getFailureState() {
		return failureState;
	}
	public void setFailureState(FailureState failureState) {
		this.failureState = failureState;
	};
	
	
}
