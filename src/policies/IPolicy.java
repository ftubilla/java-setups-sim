package policies;

import sim.*;

public interface IPolicy {

	// TODO Change logic so that you don't have to create events but only issue
	// high level commands. For example, machine.changeSetup or machine.setSprint,
	// all fire the necessary events.
	// TODO CHange "setup" to "init"
	
	public void setUp(Sim sim);
	public void updateControl(Sim sim);
	
}
