package discreteEvent;

/**
 * Each type of schedule holds a specific type(s) of events. This class also
 * defines the order in which events of different schedule types but occurring
 * at the same time should be sequenced. A dumpable schedule type is one that
 * can be cleared at any given time and doesn't affect the simulation.
 * 
 * @author ftubilla
 * 
 */
public enum ScheduleType {
	
	// Note: the order of declaration here is important because it determines which
	// type of event(s) get processed first if two or more events occur at the same time!
				    //  dumpable?      delayable?    
	DEMAND			(false,  			false),
	PRODUCTION		(false, 			true),
	CONTROL			(true, 				false),
	FAILURES		(false, 		    true),
	REPAIRS			(false,				false);

	private final boolean dumpable;		//In a dumpable schedule, we can clear the queue of events at any time
	private final boolean delayable;
	
	private ScheduleType(boolean dumpable, boolean delayable){
		this.dumpable = dumpable;
		this.delayable = delayable;
	}

	/**
	 * Use this method to determine the schedule type of a given event. Note that 
	 * each event must map one to one to a given schedule type, which is defined here.
	 * @param event
	 * @return The ScheduleType of this event
	 */
	public static ScheduleType getType(Event event){
		Class<? extends Event> eventClass = event.getClass();
		if (eventClass.equals(DemandArrival.class)){
			return DEMAND;
		} 
		if (eventClass.equals(ProductionDeparture.class)){
			return PRODUCTION;
		}
		if (eventClass.equals(ControlEvent.class) || eventClass.equals(Changeover.class)){
			return CONTROL;
		}
		if (eventClass.equals(Failure.class)){
			return FAILURES;
		} 
		if (eventClass.equals(Repair.class)){
			return REPAIRS;
		}
		else {
			throw new AssertionError("This event has no schedule type defined!");
		}
	}
	
	public boolean isDumpable(){
		return dumpable;
	}
	
	public boolean isDelayable(){
		return delayable;
	}
}