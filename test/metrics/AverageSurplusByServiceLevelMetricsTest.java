package metrics;

import static util.UtilMethods.c;

import java.util.Collections;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import lombok.extern.apachecommons.CommonsLog;
import metrics.surplusstatistics.SurplusStatistics;
import output.Recorders;
import params.Params;
import sim.Sim;
import sim.SimSetup;
import system.Item;
import util.SimBasicTest;

@CommonsLog
public class AverageSurplusByServiceLevelMetricsTest extends SimBasicTest {

    private Params params;
    private double tol = 1e-3;

    @Test
    public void testOneTimeStep() {

        // First set the surplus targets to 0
        params = Params.builder()
                .surplusTargets(c(0, 0, 0))
                .initialDemand(c(100, 100, 100))
                .demandRates(c(0, 0.1, 0.1)) // note the 0 demand rate for item 0!
                .productionRates(c(1, 1, 1))
                .initialSetup(0)
                .finalTime(99.999)
                .build();

        Sim sim = new Sim(params);
        SimSetup.setup(sim, new Recorders(Collections.emptyList()));
        sim.run(false);
        AverageSurplusByServiceLevelMetrics metrics = sim.getMetrics().getAverageSurplusByServiceLevelMetrics();

        Item item0 = sim.getMachine().getItemById(0);
        Pair<Double, SurplusStatistics> pair0 = metrics.findOptimalOffsetForServiceLevel(item0, 0.0);
        assertWithinTolerance(pair0.getRight().getAverageBacklog(), 50.0, tol);
        assertWithinTolerance(pair0.getLeft(), 0.0, tol);

        Pair<Double, SurplusStatistics> pair1 = metrics.findOptimalOffsetForServiceLevel(item0, 1.0);
        assertWithinTolerance(pair1.getRight().getAverageInventory(), 50.0, tol);
        assertWithinTolerance(pair1.getLeft(), 100.0, tol);

        Pair<Double, SurplusStatistics> pair0p5 = metrics.findOptimalOffsetForServiceLevel(item0, 0.5);
        // The average is 25/2 because half the time we have 50 of inventory and
        // half the time 50 of backlog
        assertWithinTolerance(pair0p5.getRight().getAverageInventory(), 12.5, tol);
        assertWithinTolerance(pair0p5.getRight().getAverageBacklog(), 12.5, tol);
        assertWithinTolerance(pair0p5.getLeft(), 50.0, tol);
    }

    @Test
    public void testLongerRunEndToEnd() {

        // First set the surplus targets to 0
        params = Params.builder()
                .surplusTargets(c(0, 0, 0))
                .initialDemand(c(100, 100, 100))
                .demandRates(c(0.1, 0.2, 0.3))
                .productionRates(c(1, 1, 1))
                .initialSetup(0)
                .finalTime(1_000)
                .build();

        Sim sim = new Sim(params);
        SimSetup.setup(sim, new Recorders(Collections.emptyList()));
        sim.run(false);
        AverageSurplusByServiceLevelMetrics metrics = sim.getMetrics().getAverageSurplusByServiceLevelMetrics();

        double[] serviceLevels = { 1.0, 0.85, 0.05 };
        double[] offsets = new double[3];

        // Compute the offsets
        for (int i = 0; i < 3; i++) {
            offsets[i] = metrics.findOptimalOffsetForServiceLevel(sim.getMachine().getItemById(i), serviceLevels[i])
                    .getLeft();
        }

        // Add the offsets
        params = params.toBuilder()
                .surplusTargets(c(offsets[0], offsets[1], offsets[2]))
                .initialDemand(c(-offsets[0] + 100, -offsets[1] + 100, -offsets[2] + 100))
                .build();

        Sim sim2 = new Sim(params);
        SimSetup.setup(sim2, new Recorders(Collections.emptyList()));
        sim2.run(false);

        // Now verify that we matched the service levels
        AverageSurplusMetrics metrics2 = sim2.getMetrics().getAverageSurplusMetrics();
        for (int i = 0; i < 3; i++) {
            Item item = sim2.getMachine().getItemById(i);
            log.debug(String.format("Item %s service level %.5f", item, metrics2.getServiceLevel(item)));
            assertWithinTolerance(serviceLevels[i], metrics2.getServiceLevel(item), tol);
        }

    }

}
