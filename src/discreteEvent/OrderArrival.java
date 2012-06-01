package discreteEvent;

import sim.Sim;

public class OrderArrival extends Event{
	
	private static int idCount = 0;
	
	public OrderArrival(int time){
		super(time);
		OrderArrival.idCount++;
	}
	
	@Override
	public void handle(Sim sim){
		super.handle(sim);
	}
	
	
	public static int getCount(){
		return OrderArrival.idCount;
	}

}
