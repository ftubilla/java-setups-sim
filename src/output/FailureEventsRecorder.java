package output;

import sim.Sim;
import discreteEvent.Event;
import discreteEvent.Failure;
import discreteEvent.Repair;

public class FailureEventsRecorder extends Recorder {

	int accumulatedFailures = 0;
	int accumulatedRepairs = 0;
	enum Column {SIM_ID, TIME, EVENT, ACCUMULATED_FAILURES, ACCUMULATED_REPAIRS, SETUP};
	
	public FailureEventsRecorder(){
		super("output/failure_events.txt");
		super.writeHeader(Column.class);
	}
	
	@Override
	public void recordBeforeEvent(Sim sim, Event event) {

		String eventType = "NA";
		if (event instanceof Failure) {
			accumulatedFailures++;
			eventType = "FAILURE";
		}
		if (event instanceof Repair) {
			accumulatedRepairs++;
			eventType = "REPAIR";
		}
		record(sim.getId() + " " + sim.getTime() + " " + eventType + " " + accumulatedFailures + " " + accumulatedRepairs
				+ " " + sim.getMachine().getSetup());
	}
	
}
