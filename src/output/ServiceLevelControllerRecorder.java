package output;

import discreteEvent.ControlEvent;
import discreteEvent.Event;
import policies.ServiceLevelController;
import sim.Sim;
import system.Item;

public class ServiceLevelControllerRecorder extends Recorder {

    enum Column {
        SIM_ID, TIME, ITEM, METRIC, VALUE;
    };

    public ServiceLevelControllerRecorder() {
        super("output/service_level_controller_metrics.txt");
        super.writeHeader(Column.class);
    }

    @Override
    public void recordBeforeEvent(Sim sim, Event event) {
        if ( event instanceof ControlEvent ) {
            Item currentSetup = sim.getMachine().getSetup();
            ServiceLevelController controller = sim.getPolicy().getServiceLevelController();
            if ( currentSetup != null && controller != null ) {
                Object[] row = new Object[5];
                row[0] = sim.getId();
                row[1] = sim.getClock().getTime();
                row[2] = currentSetup.getId();

                row[3] = "LEARNED_SERVICE_LEVEL";
                row[4] = controller.getLearnedServiceLevel(currentSetup);
                record(row);

                row[3] = "CONTROL";
                row[4] = controller.getControl(currentSetup);
                record(row);

                row[3] = "LEARNING_RATE";
                row[4] = controller.getLearningRate(currentSetup);
                record(row);
            }
        }
    }

}
