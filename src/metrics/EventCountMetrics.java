package metrics;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import discreteEvent.Event;
import discreteEvent.EventListener;
import sim.Sim;

public class EventCountMetrics {

    private final Map<Class<? extends Event>, Integer> transientCount;
    private final Map<Class<? extends Event>, Integer> steadyStateCount;
    private final Set<Class<? extends Event>> eventTypes;

    public EventCountMetrics(final Sim sim) {
        this.transientCount = Maps.newHashMap();
        this.steadyStateCount = Maps.newHashMap();
        this.eventTypes = Sets.newHashSet();

        sim.getListenersCoordinator().addAfterEventListener(new EventListener() {
            @Override
            public void execute(Event event, Sim sim) {
                Map<Class<? extends Event>, Integer> mapToUse;
                if ( !sim.isTimeToRecordData() ) {
                    mapToUse = transientCount;
                } else {
                    mapToUse = steadyStateCount;
                }
                eventTypes.add(event.getClass());
                mapToUse.merge(event.getClass(), 1, Integer::sum);
            }
        });
    }

    public int getTransientCount(Class<? extends Event> eventClass) {
        return this.transientCount.getOrDefault(eventClass, 0);
    }

    public int getSteadyStateCount(Class<? extends Event> eventClass) {
        return this.steadyStateCount.getOrDefault(eventClass, 0);
    }

    public Iterable<Class<? extends Event>> getEventTypes() {
        return this.eventTypes;
    }

}
