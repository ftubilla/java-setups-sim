package discreteEvent;

import org.apache.log4j.Logger;

import sim.Sim;
import sim.TimeInstant;
import system.Item;

public class Changeover extends Event {

    private static Logger logger = Logger.getLogger(Changeover.class);

    private boolean trace = logger.isTraceEnabled();

    private Item changeTo;

    // Note that this marks the start of the changeover. At the end of the
    // changeover we will call the policy.
    public Changeover(TimeInstant time, Item changeTo) {
        super(time);
        this.changeTo = changeTo;
    }

    @Override
    public void mainHandle(Sim sim) {

        // Set the changeover, delay upcoming failures/repairs, and call the
        // policy when done
        double changeoverTime;
        if ( sim.getMachine().getSetup().equals(this.changeTo) ) {
            logger.warn("Changeover will happen immediately since it's for the same item");
            changeoverTime = 0.0;
        } else {
            changeoverTime = sim.getParams().getSetupTimes().get(changeTo.getId());
        }
        if (trace) {
            logger.trace("Changing the machine's setup to Item " + changeTo.getId() + " with a changeover time "
                    + changeoverTime);
        }
        sim.getMachine().startChangeover(changeTo);
        if (trace) {
            logger.debug("Delaying all failure events by " + changeoverTime);
        }
        sim.getMasterScheduler().delayEvents(changeoverTime);
        TimeInstant controlEventTime = sim.getTime().add(changeoverTime);
        logger.trace(String.format("Scheduling a new control event for time %s", controlEventTime));
        sim.getMasterScheduler().addEvent(new ControlEvent(controlEventTime));

    }

    @Override
    public ScheduleType getScheduleType() {
        return ScheduleType.CONTROL;
    }

}
