package policies;

import java.util.Optional;

import com.google.common.collect.Table;

import discreteEvent.ControlEvent;
import sim.Sim;

public interface IPolicy {

    public void setUpPolicy(Sim sim);

    public void updateControl(Sim sim);

    public boolean isTargetBased();

    public IServiceLevelController getServiceLevelController();

    /**
     * Provides a hook for recording internal state of a policy before each occurrence of {@link ControlEvent}.
     * 
     * @return optional table with format "KEY ITEM_ID VALUE". Item id is represented as a string, so that for
     * scalar quantities NA can be returned.
     */
    default Optional<Table<String, String, Object>> getDataToRecordBeforeControl() {
        return Optional.empty();
    }

}
