package output;

import java.util.LinkedHashSet;
import java.util.Set;

import sim.Sim;
import discreteEvent.Event;



public class Recorders {

	private Set<Recorder> recorders;
	
	public Recorders() {
		recorders = new LinkedHashSet<Recorder>();
		recorders.add(new InterEventLengthsRecorder());
		recorders.add(new FailureEventsRecorder());
		recorders.add(new TimeMetricsRecorder());
		recorders.add(new ParamsRecorder());
		recorders.add(new AverageSurplusMetricsRecorder());
	}

	public synchronized void recordBeforeEvent(Sim sim, Event event){
		if (sim.isTimeToRecordData()) {
			for (Recorder recorder : recorders) {
				recorder.recordBeforeEvent(sim, event);
			}
		}
	}
	
	public synchronized void recordAfterEvent(Sim sim, Event event){
		if (sim.isTimeToRecordData()) {
			for (Recorder recorder : recorders) {
				recorder.recordAfterEvent(sim, event);
			}
		}
	}
	
	public synchronized void recordEndOfSim(Sim sim){
		if (sim.isTimeToRecordData()) {
			for (Recorder recorder : recorders) {
				recorder.recordEndOfSim(sim);
			}
		}
	}
	
	public void closeAll(){
		for (Recorder recorder : recorders){
			recorder.close();
		}
	}
			
}
