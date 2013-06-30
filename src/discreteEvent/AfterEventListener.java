package discreteEvent;

import sim.Sim;

public abstract class AfterEventListener implements IEventListener {

	private static int count=0;
		
	private int id;
		
	public AfterEventListener(){
		id=count++;
	}
		
	@Override
	public abstract void execute(Event event, Sim sim);

	@Override
	public int getId(){
		return id;
	}
	
	@Override
	public String toString(){
		return "AfterEventListener:" + id;
	}
	
}


