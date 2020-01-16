package output;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import org.apache.log4j.Logger;

import sim.Sim;
import system.Item;

import com.google.common.collect.EvictingQueue;

import discreteEvent.Event;

/**
 * Records the surplus over time. If the recordHighFreq is disabled, it will
 * record just the last N points.
 * 
 * @author ftubilla
 *
 */
public class TimeMetricsRecorder extends Recorder {

    private static Logger logger = Logger.getLogger(TimeMetricsRecorder.class);
    public static int MIN_NUM_POINTS_PER_ITEM = 750;

    private boolean trace = logger.isTraceEnabled();
    private Map<Sim, Map<Item, Queue<Object[]>>> lastNRows;

    public enum Column {
        SIM_ID, TIME, SETUP, ITEM, SURPLUS, CUM_PROD, CUM_DEM
    };

    public TimeMetricsRecorder() {
        super("output/time_metrics.txt");
        super.writeHeader(Column.class);
        lastNRows = new HashMap<Sim, Map<Item, Queue<Object[]>>>();
    }

    @Override
    public void recordAfterEvent(Sim sim, Event event) {
        if (trace) {
            logger.trace("Recording state of machine at time " + sim.getTime());
        }
        for (Item item : sim.getMachine()) {
            Object[] row = getRow(item, sim);
            record(row);
        }
    }

    @Override
    public void updateAfterEvent(Sim sim, Event event) {
        for (Item item : sim.getMachine()) {
            Object[] row = getRow(item, sim);
            // Use an evicting queue so that we maintain a fixed number of
            // points per item
            if (!lastNRows.containsKey(sim)) {
                lastNRows.put(sim, new HashMap<Item, Queue<Object[]>>());
            }
            Map<Item, Queue<Object[]>> lastNRowsForSim = lastNRows.get(sim);
            if (!lastNRowsForSim.containsKey(item)) {
                lastNRowsForSim.put(item, EvictingQueue.<Object[]> create(MIN_NUM_POINTS_PER_ITEM));
            }
            lastNRowsForSim.get(item).add(row);
        }
    }

    @Override
    public void recordEndOfSim(Sim sim) {
        // If highFreq is turned off, record the last N points stored for that
        // sim instance
        if (!sim.getParams().isRecordHighFreq()) {
            for (int i = 0; i < MIN_NUM_POINTS_PER_ITEM; i++) {
                for (Item item : sim.getMachine()) {
                    Map<Item, Queue<Object[]>> lastNRowsForSim = lastNRows.get(sim);
                    Queue<Object[]> queue = lastNRowsForSim.get(item);
                    Object[] row = queue.poll();
                    if ( row != null ) {
                        // If the number of points is too low, row could be null
                        record(row);
                    }
                }
            }
        }
        // Remove the sim instance from the map to preserve memory
        lastNRows.remove(sim);
    }

    private Object[] getRow(Item item, Sim sim) {

        Object[] row = new Object[7];
        row[0] = sim.getId();
        row[1] = sim.getTime();
        row[2] = sim.getMachine().getSetup().getId();
        row[3] = item.getId();
        row[4] = item.getSurplus();
        row[5] = item.getCumulativeProduction();
        row[6] = item.getCumulativeDemand();

        return row;
    }

}
