package output;

import metrics.AverageSurplusMetrics;

import org.apache.log4j.Logger;

import sim.Sim;
import system.Item;

public class AverageSurplusMetricsRecorder extends Recorder {
    private static Logger logger = Logger.getLogger(AverageSurplusMetricsRecorder.class);

    @SuppressWarnings("unused")
    private boolean debug = logger.isDebugEnabled();

    @SuppressWarnings("unused")
    private boolean trace = logger.isTraceEnabled();

    enum Column {
        SIM_ID, METRIC, ITEM, VALUE
    };

    public AverageSurplusMetricsRecorder() {
        super("output/average_surplus_metrics.txt");
        super.writeHeader(Column.class);
    }

    @Override
    public void recordEndOfSim(Sim sim) {

        AverageSurplusMetrics metrics = sim.getMetrics().getAverageSurplusMetrics();
        double cost = 0.0;
        Object[] row = new Object[4];
        for (Item item : sim.getMachine()) {
            row[0] = sim.getId();
            row[2] = item.getId();

            row[1] = "INVENTORY";
            row[3] = metrics.getAverageInventory(item);
            record(row);
            cost += metrics.getAverageInventory(item) * item.getInventoryCostRate();

            row[1] = "BACKLOG";
            row[3] = metrics.getAverageBacklog(item);
            record(row);
            cost += metrics.getAverageBacklog(item) * item.getBacklogCostRate();

            row[1] = "SERVICE_LEVEL";
            row[3] = metrics.getServiceLevel(item);
            record(row);
        }

        // Add the cost
        row[1] = "AVERAGE_COST";
        row[2] = "NA";
        row[3] = cost;
        record(row);

    }

}

