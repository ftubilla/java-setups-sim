package processes.production;

import org.apache.log4j.Logger;

import discreteEvent.Event;
import discreteEvent.EventListener;
import discreteEvent.ProductionDeparture;
import sim.Clock;
import sim.Sim;
import sim.TimeInstant;
import system.Item;
import system.Machine;

/**
 * Implements a continuous production processs in which the production
 * accumulated during a period DT for item i is equal to the production rate of
 * that item times DT whenever the machine is up and sprinting or the demand
 * rate times DT if the machine is cruising with i. Note that this implementor
 * *does not* generate ProductionDeparture events but instead uses
 * BeforeEventListener's to update the production.
 * 
 * @author ftubilla
 * 
 */
public class ContinuousProductionProcess implements IProductionProcess {

    private static Logger logger = Logger.getLogger(ContinuousProductionProcess.class);
    private Clock         clock;

    @Override
    public ProductionDeparture getNextProductionDeparture(Item item, TimeInstant currentTime) {
        /**
         * Because the production process is deterministic and continuous, it is
         * hard to predict when to generate a new ProductionDeparture event.
         * Thus, we do not use these events for this production process.
         */
        return null;
    }

    @Override
    public void init(final Sim sim) {
        logger.debug("Initializing continuous production process");

        this.clock = sim.getClock();
        sim.getListenersCoordinator().addBeforeEventListener(new EventListener() {
            /**
             * This listener makes sure that every time that an event occurs,
             * the production of the current setup is updated according to the
             * state of the machine.
             */
            @Override
            public void execute(Event event, Sim sim) {
                if (sim.getMachine().getFailureState() == Machine.FailureState.UP) {
                    TimeInstant deltaTime = event.getTime().subtract(sim.getTime());
                    for (Item item : sim.getMachine()) {
                        // Execute only for the item that we are currently set
                        // up for
                        if (sim.getMachine().getSetup().equals(item)) {
                            switch (sim.getMachine().getOperationalState()) {
                                case SPRINT:
                                    item.setCumulativeProduction(item.getCumulativeProduction()
                                            + item.getProductionRate() * deltaTime.doubleValue());
                                    break;
                                case CRUISE:
                                    item.setCumulativeProduction(item.getCumulativeProduction()
                                            + item.getDemandRate() * deltaTime.doubleValue());
                                    break;
                                default:
                                    // Do nothing
                            }
                        }
                    }
                }
            }
        });
    }

    @Override
    public boolean isDiscrete() {
        return false;
    }

    @Override
    public TimeInstant getNextScheduledProductionDepartureTime(Item item) {
        assert item.isUnderProduction() : "This method assumes that the item is under production!";
        return clock.getTime();
    }

}
