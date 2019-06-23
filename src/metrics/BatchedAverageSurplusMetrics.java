package metrics;

import discreteEvent.Event;
import sim.Sim;
import system.Item;

/**
 * Splits the metrics time into batches and allows for the estimation of the
 * precision in the cost measurements, based on the variability of the average
 * cost across batches. Note that this metric is not adjusting the target
 * surplus to match the desired surplus level, so it should be compared with the
 * raw costs and not the service-level adjusted costs.
 *
 */
public class BatchedAverageSurplusMetrics {

    private final AverageSurplusMetrics[] batches;
    private final Sim sim;

    public BatchedAverageSurplusMetrics(final int numBatches, final Sim sim) {
        this.batches = new AverageSurplusMetrics[numBatches];
        this.sim = sim;
        double metricsStartTime = sim.getParams().getMetricsStartTime();
        double finalTime = sim.getParams().getFinalTime();
        double recordTime = finalTime - metricsStartTime;
        double batchTime = recordTime / (double) numBatches;
        for ( int i = 0; i < numBatches; i++ ) {
            double from = metricsStartTime + i * batchTime;
            double to = from + batchTime;
            TimeSegmentedAverageSurplusMetrics batchMetrics =
                    new TimeSegmentedAverageSurplusMetrics(sim, from, to);
            this.batches[i] = batchMetrics;
        }
    }

    public class TimeSegmentedAverageSurplusMetrics extends AverageSurplusMetrics {

        private final double from;
        private final double to;

        public TimeSegmentedAverageSurplusMetrics(Sim sim, double from, double to) {
            super(sim);
            this.from = from;
            this.to = to;
        }

        @Override
        protected boolean canRecordEvent(Event event, Sim sim) {
            return sim.getClock().hasPassedEpoch(this.from) && 
                    ! sim.getClock().hasReachedEpoch(this.to);
        }

    }

    public double[] getBatchedAverageCosts() {
        double[] costs = new double[this.batches.length];
        for ( int i = 0; i < costs.length; i++ ) {
            AverageSurplusMetrics batchMetrics = this.batches[i];
            double cost = 0;
            for ( Item item : this.sim.getMachine() ) {
                double aveInventory = batchMetrics.getAverageInventory(item);
                double aveBacklog = batchMetrics.getAverageBacklog(item);
                cost += aveInventory * item.getInventoryCostRate() + aveBacklog * item.getBacklogCostRate();
            }
            costs[i] = cost;
        }
        return costs;
    }

}
