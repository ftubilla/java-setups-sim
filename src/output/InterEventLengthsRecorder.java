package output;

import discreteEvent.Event;
import discreteEvent.ScheduleType;
import sim.Sim;
import sim.TimeInstant;

public class InterEventLengthsRecorder extends Recorder {

    enum Column {
        SIM_ID, EVENT, DURATION
    };

    public InterEventLengthsRecorder() {
        super("output/inter_event_times.txt");
        super.writeHeader(Column.class);
    }

    @Override
    public void recordBeforeEvent(Sim sim, Event event) {
        ScheduleType st = event.getScheduleType();
        if (st == ScheduleType.FAILURES || st == ScheduleType.REPAIRS) {
            record(sim.getId(), event.getClass().getSimpleName(),
                    sim.getMasterScheduler().getSchedule(event.getScheduleType()).getLastInterEventTime());
        }

    }

    private void record(int simId, String eventName, TimeInstant duration) {
        super.record(simId + " " + eventName.toUpperCase() + " " + duration);
    }

}
