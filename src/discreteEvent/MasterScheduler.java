package discreteEvent;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.log4j.Logger;

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

	private Map<ScheduleType, Schedule> schedules;
	private Queue<IScheduleTrigger> scheduleTriggers;

	public MasterScheduler(Sim sim) {

		logger.debug("Creating the master schedule and initializing each schedule");
		schedules = new HashMap<ScheduleType, Schedule>();
		for (ScheduleType st : ScheduleType.values()) {
			schedules.put(st, new Schedule(st));
		}
		scheduleTriggers = new LinkedList<IScheduleTrigger>();

	}

	/**
	 * Add an Event e to the master schedule. The type of the event is
	 * determined by the method based on the event's class, as defined in
	 * Schedule Type.
	 * 
	 * @param Event
	 *            to add
	 */
	public void addEvent(Event e) {
		if (e != null) {
			logger.debug("Adding event " + e.getId()
					+ " to the master schedule");
			schedules.get(ScheduleType.getType(e)).addEvent(e);

			// Since the triggers can add new triggers, need to loop only over
			// the current triggers
			int currentTriggers = scheduleTriggers.size();
			for (int i = 0; i < currentTriggers; i++) {
				logger.debug("Calling trigger "
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
		logger.debug("Next event occuring is of schedule type " + nextType
				+ " and occurs at " + nextEventTime);
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
		logger.debug("Next event occuring is of type " + nextType
				+ " and occurs at " + nextEventTime);
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
				schedules.get(st).delayEvents(delay);
			}
		}
	}

	/**
	 * Adds a trigger to the schedule, which will be called (and removed)
	 * whenever a new event is added.
	 */
	public void addTrigger(IScheduleTrigger scheduleTrigger) {
		logger.debug("Adding trigger " + scheduleTrigger.getId());
		scheduleTriggers.add(scheduleTrigger);
	}
}
