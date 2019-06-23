package output;

import metrics.BatchedAverageSurplusMetrics;
import sim.Sim;

public class BatchedAverageSurplusMetricsRecorder extends Recorder {

    enum Column {
        SIM_ID, BATCH, COST
    };

    public BatchedAverageSurplusMetricsRecorder() {
        super("output/batched_average_surplus_metrics.txt");
        super.writeHeader(Column.class);
    }

    @Override
    public void recordEndOfSim(Sim sim) {
        BatchedAverageSurplusMetrics metrics = sim.getMetrics().getBatchedAverageSurplusMetrics();
        double[] costs = metrics.getBatchedAverageCosts();
        for ( int i = 0; i < costs.length; i++ ) {
            Object[] row = new Object[Column.values().length];
            row[0] = sim.getId();
            row[1] = i;
            row[2] = costs[i];
            record(row);
        }
    }

}
