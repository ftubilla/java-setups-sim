package output;

import discreteEvent.ControlEvent;
import discreteEvent.Event;
import policies.IServiceLevelController;
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
            recordRow(sim, sim.getMachine().getSetup());
        }
    }

    @Override
    public void recordEndOfSim(Sim sim) {
        for ( Item item : sim.getMachine() ) {
            recordRow(sim, item);
        }
    }

    private void recordRow(Sim sim, Item item) {
        IServiceLevelController controller = sim.getPolicy().getServiceLevelController();
        if (item != null && controller != null) {
            Object[] row = new Object[5];
            row[0] = sim.getId();
            row[1] = sim.getClock().getTime();
            row[2] = item.getId();

            row[3] = "LEARNED_SERVICE_LEVEL";
            row[4] = controller.getLearnedServiceLevel(item);
            record(row);

            row[3] = "CONTROL";
            row[4] = controller.getControl(item);
            record(row);

            row[3] = "LEARNING_RATE";
            row[4] = controller.getLearningRate(item);
            record(row);
        }
    }

}
