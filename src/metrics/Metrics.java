package metrics;

import lombok.Getter;
import sim.Sim;

@Getter
public class Metrics {

    private final TimeFractionsMetrics                timeFractionsMetrics;
    private final AverageSurplusMetrics               averageSurplusMetrics;
    private final AverageSurplusByServiceLevelMetrics averageSurplusByServiceLevelMetrics;
    private final BatchedAverageSurplusMetrics batchedAverageSurplusMetrics;

    public Metrics(Sim sim) {
        this.timeFractionsMetrics = new TimeFractionsMetrics(sim);
        this.averageSurplusMetrics = new AverageSurplusMetrics(sim);
        this.averageSurplusByServiceLevelMetrics = new AverageSurplusByServiceLevelMetrics(sim);
        this.batchedAverageSurplusMetrics = new BatchedAverageSurplusMetrics(sim.getParams().getNumBatchesForBatchedMetrics(), sim);
    }

}
