package discreteEvent;

import sim.Sim;

public abstract class EventListener implements IEventListener {

	private static int count=0;
	
	private int id;
	
	public EventListener(){
		id=count++;
	}
	
	public abstract void execute(Event event, Sim sim);

	@Override
	public int getId(){
		return id;
	}
	
	@Override
	public String toString(){
		return "EventListener:" + id;
	}
}
