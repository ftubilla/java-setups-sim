package output;

import discreteEvent.Event;
import metrics.EventCountMetrics;
import sim.Sim;

public class EventCountMetricsRecorder extends Recorder {

    enum Column { SIM_ID, EVENT, PERIOD, COUNT };

    public EventCountMetricsRecorder() {
        super("output/event_counts.txt");
        super.writeHeader(Column.class);
    }

    @Override
    public void recordEndOfSim(Sim sim) {
        EventCountMetrics metrics = sim.getMetrics().getEventCountMetrics();
        for (Class<? extends Event> eventType : metrics.getEventTypes()) {
            Object[] row = new Object[4];
            row[0] = sim.getId();
            row[1] = eventType.getSimpleName();

            // Transient
            row[2] = "TRANSIENT";
            row[3] = metrics.getTransientCount(eventType);
            record(row);

            // Steady-state
            row[2] = "STEADY_STATE";
            row[3] = metrics.getSteadyStateCount(eventType);
            record(row);
        }
    }

}
