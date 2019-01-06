package metrics;

import lombok.Getter;
import sim.Sim;

@Getter
public class Metrics {

    private TimeFractionsMetrics                timeFractionsMetrics;
    private AverageSurplusMetrics               averageSurplusMetrics;
    private AverageSurplusByServiceLevelMetrics averageSurplusByServiceLevelMetrics;

    public Metrics(Sim sim) {
        timeFractionsMetrics = new TimeFractionsMetrics(sim);
        averageSurplusMetrics = new AverageSurplusMetrics(sim);
        averageSurplusByServiceLevelMetrics = new AverageSurplusByServiceLevelMetrics(sim);
    }

}
