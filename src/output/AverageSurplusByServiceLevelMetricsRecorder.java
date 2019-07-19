package output;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import metrics.AverageSurplusByServiceLevelMetrics;
import metrics.surplusstatistics.SurplusStatistics;
import sim.Sim;
import system.Item;

public class AverageSurplusByServiceLevelMetricsRecorder extends Recorder {
    private static Logger logger = Logger.getLogger(AverageSurplusByServiceLevelMetricsRecorder.class);

    @SuppressWarnings("unused")
    private boolean debug = logger.isDebugEnabled();

    @SuppressWarnings("unused")
    private boolean trace = logger.isTraceEnabled();

    enum Column {
        SIM_ID, METRIC, ITEM, VALUE
    };

    public AverageSurplusByServiceLevelMetricsRecorder() {
        super("output/average_surplus_by_service_level_metrics.txt");
        super.writeHeader(Column.class);
    }

    @Override
    public void recordEndOfSim(Sim sim) {

        AverageSurplusByServiceLevelMetrics metrics = sim.getMetrics().getAverageSurplusByServiceLevelMetrics();

        double cost = 0.0;
        Object[] row = new Object[4];
        for (Item item : sim.getMachine()) {

            double itemCost = 0.0;
            row[0] = sim.getId();
            row[2] = item.getId();

            double desiredServiceLevel = sim.getDerivedParams().getServiceLevels().get(item.getId());
            Pair<Double, SurplusStatistics> offsetPair = metrics.findOptimalOffsetForServiceLevel(item, desiredServiceLevel);

            row[1] = "DESIRED_SERVICE_LEVEL";
            row[3] = desiredServiceLevel;
            record(row);

            row[1] = "OPTIMAL_SURPLUS_OFFSET";
            row[3] = offsetPair.getLeft();
            record(row);

            row[1] = "OPTIMAL_SURPLUS_TARGET";
            row[3] = item.getSurplusTarget() + offsetPair.getLeft();
            record(row);

            SurplusStatistics stats = offsetPair.getRight();
            row[1] = "INVENTORY";
            row[3] = stats.getAverageInventory();
            record(row);
            itemCost += stats.getAverageInventory() * item.getInventoryCostRate();

            row[1] = "BACKLOG";
            row[3] = stats.getAverageBacklog();
            record(row);
            itemCost += stats.getAverageBacklog() * item.getBacklogCostRate();

            row[1] = "ITEM_AVERAGE_COST";
            row[3] = itemCost;
            record(row);

            row[1] = "SERVICE_LEVEL";
            row[3] = stats.getServiceLevel();
            record(row);

            cost += itemCost;
        }

        row[1] = "AVERAGE_COST";
        row[2] = "NA";
        row[3] = cost;
        record(row);

    }

}
