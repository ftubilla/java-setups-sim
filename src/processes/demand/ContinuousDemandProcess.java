package processes.demand;

import org.apache.log4j.Logger;

import discreteEvent.DemandArrival;
import discreteEvent.Event;
import discreteEvent.EventListener;
import sim.Clock;
import sim.Sim;
import sim.TimeInstant;
import system.Item;

/**
 * Implements a continuous demand process in which the demand accumulated during
 * an interval DT for product i is equal to the demand rate d_i times DT. Note
 * that this class *does not* generate DemandArrival events but instead uses
 * BeforeEventListeners to update the demand.
 * 
 * @author ftubilla
 * 
 */
public class ContinuousDemandProcess implements IDemandProcess {

    private static Logger logger = Logger.getLogger(ContinuousDemandProcess.class);

    private Clock clock;

    @Override
    public DemandArrival getNextDemandArrival(Item item, TimeInstant currentTime) {
        // Because the demand is continuous, we would have to either generate
        // demand arrival events every infinitesimal interval or exactly at the
        // moment at which other events occur. However, the latter approach is
        // better done through BeforeEventListeners.
        //
        return null;
    }

    @Override
    public void init(final Sim sim) {

        logger.debug("Initializing continuous demand process. Adding a BeforeEventListener to update the demand "
                + "at the beginning of each event.");
        this.clock = sim.getClock();

        sim.getListenersCoordinator().addBeforeEventListener(new EventListener() {

            private TimeInstant latestUpdateTime = new TimeInstant(0.0);

            // This function will be executed before handling any event, so that
            // it is guaranteed that we start the handling of the event with
            // updated demand info.
            @Override
            public void execute(Event event, Sim sim) {
                TimeInstant deltaTime =  event.getTime().subtract(latestUpdateTime);
                if (deltaTime.hasPassedEpoch(0)) {
                    for (Item item : sim.getMachine()) {
                        item.setCumulativeDemand(item.getCumulativeDemand() + item.getDemandRate() * deltaTime.doubleValue());
                    }
                    latestUpdateTime = event.getTime();
                }
            }
        });

    }

    @Override
    public boolean isDiscrete() {
        return false;
    }

    @Override
    public TimeInstant getNextScheduledDemandArrivalTime(Item item) {
        // Under the continuous model, we get an infinitesimal demand arrival at
        // every second.
        return clock.getTime();
    }

}
