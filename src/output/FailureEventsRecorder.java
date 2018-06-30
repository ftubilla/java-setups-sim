package output;

import sim.Sim;
import discreteEvent.Event;
import discreteEvent.Failure;
import discreteEvent.Repair;

public class FailureEventsRecorder extends Recorder {

    int accumulatedFailures = 0;
    int accumulatedRepairs  = 0;

    enum Column {
        SIM_ID, TIME, EVENT, ACCUMULATED_FAILURES, ACCUMULATED_REPAIRS, SETUP, IS_UP
    };

    public FailureEventsRecorder() {
        super("output/failure_events.txt");
        super.writeHeader(Column.class);
    }

    @Override
    public void recordAfterEvent(Sim sim, Event event) {

        String eventType = event.getClass().getSimpleName().toUpperCase();
        if (event instanceof Failure) {
            accumulatedFailures++;
        }
        if (event instanceof Repair) {
            accumulatedRepairs++;
        }

        record(sim.getId() + " " + sim.getTime() + " " + eventType + " " + accumulatedFailures + " "
                + accumulatedRepairs + " " + sim.getMachine().getSetup() + " " + sim.getMachine().isUp());
    }

}
