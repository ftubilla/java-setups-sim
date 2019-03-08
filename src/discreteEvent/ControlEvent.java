package discreteEvent;

import org.apache.log4j.Logger;

import sim.Sim;
import sim.TimeInstant;

public class ControlEvent extends Event {

    private static Logger logger = Logger.getLogger(ControlEvent.class);

    public ControlEvent(TimeInstant time) {
        super(time);
    }

    @Override
    public void mainHandle(Sim sim) {
        // Because any other scheduled Control Event will be redundant after
        // handling this one, we clear the queue
        sim.getMasterScheduler().dumpEvents();
        logger.debug("Processing " + this);
        sim.getPolicy().updateControl(sim);
    }

    @Override
    public ScheduleType getScheduleType() {
        return ScheduleType.CONTROL;
    }

}
