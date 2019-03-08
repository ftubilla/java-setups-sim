package discreteEvent;

import org.apache.log4j.Logger;

import sim.Sim;
import sim.TimeInstant;

public class Repair extends Event {

    private static Logger logger = Logger.getLogger(Repair.class);

    public Repair(TimeInstant time) {
        super(time);
    }

    @Override
    public void mainHandle(Sim sim) {

        sim.getMachine().repair();
        sim.getPolicy().updateControl(sim);

        // Generate the next failure
        double nextTimeToFailure = sim.getTheFailuresGenerator().nextTimeInterval();
        sim.getMasterScheduler().addEvent(new Failure(sim.getTime().add(nextTimeToFailure)));
        logger.debug("Finished repairing the machine. Next TTF is " + nextTimeToFailure);
    }

    @Override
    public ScheduleType getScheduleType() {
        return ScheduleType.REPAIRS;
    }

}
