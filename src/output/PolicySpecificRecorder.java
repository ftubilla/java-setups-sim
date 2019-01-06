package output;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.google.common.collect.Table;

import discreteEvent.ControlEvent;
import discreteEvent.Event;
import sim.Sim;

/**
 * A general purporse key-value table for recording policy-specific data.
 */
public class PolicySpecificRecorder extends Recorder {

    enum Column {
        SIM_ID, TIME, KEY, ITEM, VALUE
    };

    public PolicySpecificRecorder() {
        super("output/policy_specific_metrics.txt");
        super.writeHeader(Column.class);
    }

    @Override
    public void recordBeforeEvent(Sim sim, Event event) {
        if ( event instanceof ControlEvent ) {
            Optional<Table<String, String, Object>> optionalData = sim.getPolicy().getDataToRecordBeforeControl();
            if ( optionalData.isPresent() ) {
                Table<String, String, Object> data = optionalData.get();
                Object[] row = new Object[5];
                row[0] = sim.getId();
                row[1] = sim.getTime();
                for ( String key : data.rowKeySet() ) {
                    Map<String, Object> value = data.row(key);
                    row[2] = key;
                    for ( Entry<String, Object> entry : value.entrySet() ) {
                        row[3] = entry.getKey();
                        row[4] = entry.getValue();
                        record(row);
                    }
                }
            }
        }
    }

}
