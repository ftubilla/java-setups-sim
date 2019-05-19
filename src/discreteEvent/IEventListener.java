package discreteEvent;

import sim.Sim;

/**
 * An interface for closures that can be called before or after an event occurs.
 * Note: One shouldn't rely on the order of execution of different listeners
 * because this is determined by the order in which they are added, which might
 * change over time as the code base grows.
 * 
 * @author ftubilla
 * 
 */
public interface IEventListener {

    /**
     * The method called when the event occurs.
     * 
     * @param event - the event that triggered the listener
     */
    public void execute(Event event, Sim sim);

    public int getId();
}
