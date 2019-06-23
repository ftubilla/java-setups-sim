package metrics;

import java.util.HashMap;
import java.util.Map;

import discreteEvent.Event;
import discreteEvent.EventListener;
import lombok.extern.apachecommons.CommonsLog;
import metrics.surplusstatistics.StreamSurplusStatisticsCalculator;
import sim.Sim;
import system.Item;
import system.Machine;

@CommonsLog
public class AverageSurplusMetrics {

    private Map<Item, StreamSurplusStatisticsCalculator> surplusStatsCalculators;
    private Machine                                      machine;

    public AverageSurplusMetrics(Sim sim) {

        // Initialize data structures
        surplusStatsCalculators = new HashMap<>();
        machine = sim.getMachine();
        for (Item item : machine) {
            surplusStatsCalculators.put(item, new StreamSurplusStatisticsCalculator());
        }

        sim.getListenersCoordinator().addAfterEventListener(new EventListener() {
            @Override
            public void execute(Event event, Sim sim) {

                if ( canRecordEvent(event, sim) ) {
                    log.trace("Recording data point");
                    for (Item item : machine) {
                        StreamSurplusStatisticsCalculator calculator = surplusStatsCalculators.get(item);
                        calculator.addPoint(sim.getTime(), item.getSurplus());
                    }
                }
            }
        });
    }

    protected boolean canRecordEvent(Event event, Sim sim) {
        return sim.isTimeToRecordData();
    }

    public double getAverageInventory(Item item) {
        return surplusStatsCalculators.get(item).getAverageInventory();
    }

    public double getAverageBacklog(Item item) {
        return surplusStatsCalculators.get(item).getAverageBacklog();
    }

    public double getServiceLevel(Item item) {
        return surplusStatsCalculators.get(item).getServiceLevel();
    }

    public double getMinSurplusLevel(Item item) {
        return surplusStatsCalculators.get(item).getMinSurplus();
    }

}
