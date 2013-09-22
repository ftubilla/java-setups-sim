package output;

import sim.Sim;
import discreteEvent.Event;
import discreteEvent.ScheduleType;


public class InterEventLengthsRecorder extends Recorder {

	enum Column {SIM_ID, EVENT, DURATION};
	
	public InterEventLengthsRecorder(){
		super("output/inter_event_times.txt");
		super.writeHeader(Column.class);		
	}
	
	@Override
	public void recordBeforeEvent(Sim sim, Event event) {
		ScheduleType st = ScheduleType.getType(event);
		if (st == ScheduleType.FAILURES || st == ScheduleType.REPAIRS) {
			record(sim.getId(), event.getClass().getSimpleName(),
					sim.getMasterScheduler().getSchedule(ScheduleType.getType(event)).getLastInterEventTime());
		}

	}
		
	private void record(int simId, String eventName, double duration){
		super.record(simId + " " + eventName.toUpperCase() + " " + duration);
	}
	
		
}
