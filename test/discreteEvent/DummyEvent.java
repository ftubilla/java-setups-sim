package discreteEvent;

import sim.Sim;
import sim.TimeInstant;

/**
 * An instance of {@link Event} meant for testing.
 *  
 */
public class DummyEvent extends Event {

    private final ScheduleType type;

    public DummyEvent(double time, ScheduleType type) {
        super(new TimeInstant(time));
        this.type = type;
    }

    @Override
    protected void mainHandle(Sim sim) {
        // Do nothing
    }

    @Override
    public ScheduleType getScheduleType() {
        return this.type;
    }

}
