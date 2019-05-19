package discreteEvent;

/**
 * 
 * A ScheduleTrigger provides a trigger method that is called the next time that
 * the master scheduler gets a new event added. This is useful for ensuring, for
 * e.g., that whenever some event is added to the master schedule, an
 * accompanying event is also included.
 * 
 * @author ftubilla
 * 
 */
public interface IScheduleTrigger {

    public void trigger(Event eventAdded);

    public int getId();
}
