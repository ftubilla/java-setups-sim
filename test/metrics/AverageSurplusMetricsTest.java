package metrics;

import static util.UtilMethods.c;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import output.Recorders;
import params.Params;
import sim.Sim;
import sim.SimSetup;
import system.Item;
import util.SimBasicTest;

public class AverageSurplusMetricsTest extends SimBasicTest {

    private Params params;

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testOneTimeStep() {

        // First set the surplus targets to 0
        params = Params.builder()
                .surplusTargets(c(0, 0, 0))
                .initialDemand(c(100, 100, 100))
                .demandRates(c(0, 0.1, 0.1)) // Note the 0 demand rate for item 0!
                .productionRates(c(1, 1, 1))
                .initialSetup(0)
                .finalTime(99.9999)
                .metricsStartTime(0)
                .build();

        Sim sim = new Sim(params);
        SimSetup.setup(sim, new Recorders(Collections.emptyList()));
        sim.run(false);
        AverageSurplusMetrics metrics = sim.getMetrics().getAverageSurplusMetrics();
        Item item0 = sim.getMachine().getItemById(0);
        Item item1 = sim.getMachine().getItemById(1);
        assertWithinTolerance(metrics.getAverageBacklog(item0), 50.0, 1e-3);
        assertWithinTolerance(metrics.getAverageInventory(item0), 0.0, 1e-3);
        assertWithinTolerance(metrics.getServiceLevel(item0), 0.0, 1e-3);
        assertWithinTolerance(metrics.getMinSurplusLevel(item0), -100.0, 1e-3);
        assertWithinTolerance(metrics.getMinSurplusLevel(item1), -110.0, 1e-3);

        // Now run longer and with inventory during the second half
        params = params.toBuilder().surplusTargets(c(100, 100, 100)).finalTime(200).build();
        sim = new Sim(params);
        SimSetup.setup(sim, new Recorders(Collections.emptyList()));
        sim.run(false);
        metrics = sim.getMetrics().getAverageSurplusMetrics();
        item0 = sim.getMachine().getItemById(0);
        assertWithinTolerance(metrics.getAverageBacklog(item0), 25.0, 1e-3);
        assertWithinTolerance(metrics.getAverageInventory(item0), 25.0, 1e-3);
        assertWithinTolerance(metrics.getServiceLevel(item0), 0.5, 1e-3);

    }

}
