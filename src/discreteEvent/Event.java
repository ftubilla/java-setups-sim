/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * � 2012 Fernando Tubilla. All rights reserved.
 */

package discreteEvent;

import org.apache.log4j.Logger;

import sim.Sim;
import sim.TimeInstant;

/**
 * The base class from which any other type of event should inherit. Takes care
 * of calling listeners (if any) at the beginning of the processing of the
 * event.
 * 
 * @author ftubilla
 * 
 */
public abstract class Event implements Comparable<Event> {

    private static Logger logger = Logger.getLogger(Event.class);

    protected TimeInstant time;
    protected double      deltaTime;
    private long          id;
    private static long   idCount = 0;

    public Event(final TimeInstant time) {
        this.time = time;
        this.id = idCount;
        Event.idCount++;
        logger.trace("Created " + this);
    }

    public int compareTo(Event otherEvent) {
        return this.time.compareTo(otherEvent.time);
    }

    public void handle(Sim sim) {
        beforeHandle(sim);
        if (logger.isDebugEnabled()) {
            logger.trace("Handling " + this);
        }
        deltaTime = this.time.subtract(sim.getTime()).doubleValue();
        // Advance time
        logger.trace("Advancing sim time from " + sim.getTime() + " to " + time);
        sim.setTime(time);
        mainHandle(sim);
        sim.setLatestEvent(this);
        afterHandle(sim);
    }

    // This is the main method that each new event should override
    protected abstract void mainHandle(Sim sim);

    public abstract ScheduleType getScheduleType();

    private void beforeHandle(Sim sim) {
        for (IEventListener listener : sim.getListenersCoordinator().getBeforeEventListeners()) {
            logger.trace("Executing " + listener + " for " + this);
            listener.execute(this, sim);
        }
    }

    private void afterHandle(Sim sim) {
        for (IEventListener listener : sim.getListenersCoordinator().getAfterEventListener()) {
            logger.trace("Executing " + listener);
            listener.execute(this, sim);
        }
    }

    public TimeInstant getTime() {
        return time;
    }

    protected void updateTime(TimeInstant time) {
        if (logger.isTraceEnabled()) {
            logger.trace("Updating " + this + " time to " + time);
        }
        this.time = time;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ":" + id + " (" + time + ")";
    }

}
