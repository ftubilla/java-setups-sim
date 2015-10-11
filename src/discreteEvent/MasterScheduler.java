package discreteEvent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.log4j.Logger;

import sim.Clock;
import sim.Sim;

/**
 * The main schedule object used to get access and update events in the
 * schedules.
 * 
 * @author ftubilla
 * 
 */
public class MasterScheduler {

	private static Logger logger = Logger.getLogger(MasterScheduler.class);

	
	private boolean trace = logger.isTraceEnabled();
	private Map<ScheduleType, Schedule> schedules;
	private Queue<IScheduleTrigger> scheduleTriggers;
	private Clock clock;

	public MasterScheduler(Sim sim) {

		logger.debug("Creating the master schedule and initializing each schedule");
		schedules = new HashMap<ScheduleType, Schedule>();
		for (ScheduleType st : ScheduleType.values()) {
			schedules.put(st, new Schedule(st, sim.getClock()));
		}
		scheduleTriggers = new LinkedList<IScheduleTrigger>();
		this.clock = sim.getClock(); 

	}

	/**
	 * Add an Event e to the master schedule. The type of the event is
	 * determined by the method based on the event's class, as defined in
	 * Schedule Type.
	 * 
	 * @param Event to add
	 */
	public void addEvent(Event e) {			
		if (e != null) {
			assert e.time >= clock.getTime() : "Cannot add events that occur in the past!";
			logger.trace("Adding event " + e
					+ " to the master schedule");
			schedules.get(e.getScheduleType()).addEvent(e);

			// Since the triggers can add new triggers, need to loop only over
			// the current triggers
			int currentTriggers = scheduleTriggers.size();
			for (int i = 0; i < currentTriggers; i++) {
				logger.trace("Calling trigger "
						+ scheduleTriggers.peek().getId());
				scheduleTriggers.poll().trigger(e);
			}
		}
	}

	/**
	 * Gets the next event across all schedule types. Note that ties are broken
	 * by the order of declaration of schedule types in ScheduleType.
	 * 
	 * @return The next event
	 */
	public Event getNextEvent() {

		ScheduleType nextType = null;
		double nextEventTime = Double.MAX_VALUE;

		for (ScheduleType st : ScheduleType.values()) {
			if (!schedules.get(st).eventsComplete()
					&& schedules.get(st).nextEventTime() < nextEventTime) {
				// Note that because we use a < sign here, if two or more
				// events occur at the same time, the first schedule type wins.
				nextType = st;
				nextEventTime = schedules.get(st).nextEventTime();
			}
		}
		if (trace) {
			logger.trace("Next event occurring is of schedule type " + nextType
				+ " and occurs at " + nextEventTime);
		}
		return (schedules.get(nextType).getNextEvent());
	}

	/**
	 * Returns the time at which the next event will occur. Ties are broken by
	 * the order in which the schedule types are declared in ScheduleType.
	 * 
	 * @return
	 */
	public double nextEventTime() {

		ScheduleType nextType = null;
		double nextEventTime = Double.MAX_VALUE;

		for (ScheduleType st : ScheduleType.values()) {
			if (!schedules.get(st).eventsComplete()
					&& schedules.get(st).nextEventTime() < nextEventTime) {
				nextType = st;
				nextEventTime = schedules.get(st).nextEventTime();
			}
		}
		if (trace) {
			logger.trace("Next event occuring is of type " + nextType
				+ " and occurs at " + nextEventTime);
		}
		return (schedules.get(nextType).nextEventTime());
	}

	/**
	 * This method should only be used when the same effect cannot be
	 * accomplished using other of the supplied methods in the class.
	 * 
	 * @param scheduleType
	 * @return schedule
	 */
	public Schedule getSchedule(ScheduleType st) {
		return schedules.get(st);
	}

	/**
	 * Determines if all schedules are complete or if any of the schedules has
	 * pending events to complete
	 * 
	 * @return true if all schedules are complete
	 */
	public boolean eventsComplete() {
		for (ScheduleType st : ScheduleType.values()) {
			if (!schedules.get(st).eventsComplete()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Dumps all events from schedules that are dumpable
	 */
	public void dumpEvents() {
		for (ScheduleType st : ScheduleType.values()) {
			if (st.isDumpable()) {
				if (trace) {logger.trace("Dumping events on " + st);}
				schedules.get(st).dumpEvents();
			}
		}
	}

	/**
	 * Delays all events in schedules that are delayable.
	 * 
	 * @param delay
	 */
	public void delayEvents(double delay) {
		for (ScheduleType st : ScheduleType.values()) {
			if (st.isDelayable()) {
				if (trace) {logger.trace("Delaying events on " + st);}
				schedules.get(st).delayEvents(delay);
			}
		}
	}
	
	/**
	 * Holds all schedules that are delayable.
	 */
	public void holdDelayableEvents(){
		for (ScheduleType st : ScheduleType.values()){
			if (st.isDelayable()){
				if (trace) {logger.trace("Holding events on " + st);}
				schedules.get(st).holdEvents();
			}
		}
	}
	
	/**
	 * Releases all events that were held and delays them.
	 */
	public void releaseAndDelayEvents(){
		for (ScheduleType st : ScheduleType.values()){
			if (st.isDelayable() && schedules.get(st).isOnHold()){
				if (trace) {logger.trace("Releasing events on " + st);}
				schedules.get(st).releaseAndDelayEvents();
			}
		}
	}
	

	/**
	 * Adds a trigger to the schedule, which will be called (and removed)
	 * whenever a new event is added.
	 */
	public void addTrigger(IScheduleTrigger scheduleTrigger) {
		logger.trace("Adding trigger " + scheduleTrigger.getId());
		scheduleTriggers.add(scheduleTrigger);
	}
}
