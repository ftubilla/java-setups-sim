package output;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import discreteEvent.Event;
import sim.Sim;

public class Recorders {

    private Set<Recorder> recorders = new LinkedHashSet<Recorder>();

    public Recorders() {
        recorders.add(new InterEventLengthsRecorder());
        recorders.add(new FailureEventsRecorder());
        recorders.add(new TimeMetricsRecorder());
        recorders.add(new ParamsRecorder());
        recorders.add(new AverageSurplusMetricsRecorder());
        recorders.add(new AverageSurplusByServiceLevelMetricsRecorder());
        recorders.add(new TimeFractionsRecorder());
        recorders.add(new PolicySpecificRecorder());
        recorders.add(new BatchedAverageSurplusMetricsRecorder());
        recorders.add(new ServiceLevelControllerRecorder());
        recorders.add(new EventCountMetricsRecorder());
    }

    public Recorders(Collection<Recorder> recordersCollection) {
        recorders.addAll(recordersCollection);
    }

    public synchronized void updateBeforeEvent(Sim sim, Event event) {
        for (Recorder recorder : recorders) {
            recorder.updateBeforeEvent(sim, event);
        }
    }

    public synchronized void recordBeforeEvent(Sim sim, Event event) {
        if (sim.isTimeToRecordData()) {
            for (Recorder recorder : recorders) {
                if (sim.getParams().isRecordHighFreq()) {
                    recorder.recordBeforeEvent(sim, event);
                }
            }
        }
    }

    public synchronized void updateAfterEvent(Sim sim, Event event) {
        for (Recorder recorder : recorders) {
            recorder.updateAfterEvent(sim, event);
        }
    }

    public synchronized void recordAfterEvent(Sim sim, Event event) {
        if (sim.isTimeToRecordData()) {
            for (Recorder recorder : recorders) {
                if (sim.getParams().isRecordHighFreq()) {
                    recorder.recordAfterEvent(sim, event);
                }
            }
        }
    }

    public synchronized void recordEndOfSim(Sim sim) {
        if (sim.isTimeToRecordData()) {
            for (Recorder recorder : recorders) {
                recorder.recordEndOfSim(sim);
            }
        }
    }

    public void closeAll() {
        for (Recorder recorder : recorders) {
            recorder.close();
        }
    }

}
