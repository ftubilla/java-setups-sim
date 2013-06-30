package discreteEvent;

import sim.Sim;

public abstract class BeforeEventListener implements IEventListener {

	private static int count=0;
	
	private int id;
	
	public BeforeEventListener(){
		id=count++;
	}
	
	public abstract void execute(Event event, Sim sim);

	@Override
	public int getId(){
		return id;
	}
	
	@Override
	public String toString(){
		return "BeforeEventListener:" + id;
	}
}
