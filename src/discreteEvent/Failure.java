package discreteEvent;

import org.apache.log4j.Logger;

import sim.Sim;
import sim.TimeInstant;

public class Failure extends Event {

    private static Logger logger = Logger.getLogger(Failure.class);

    public Failure(TimeInstant time) {
        super(time);
    }

    @Override
    public void mainHandle(Sim sim) {

        // Repair machine and delay the production schedule
        double repairTime = sim.getTheRepairsGenerator().nextTimeInterval();
        logger.debug("Processing failure event. Machine will be repaired after " + repairTime + " time units");
        sim.getMasterScheduler().addEvent(new Repair(sim.getTime().add(repairTime)));
        sim.getMachine().breakDown();
        sim.getPolicy().updateControl(sim);
    }

    @Override
    public ScheduleType getScheduleType() {
        return ScheduleType.FAILURES;
    }

}
