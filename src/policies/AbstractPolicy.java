package policies;

import org.apache.log4j.Logger;

import discreteEvent.Changeover;
import discreteEvent.ControlEvent;
import lombok.Getter;
import lombok.NonNull;
import lowerbounds.SurplusCostLowerBound;
import params.PolicyParams;
import sim.Clock;
import sim.Sim;
import sim.TimeInstant;
import system.Item;
import system.Machine;

public abstract class AbstractPolicy implements IPolicy {

    private static Logger logger = Logger.getLogger(AbstractPolicy.class);

    @SuppressWarnings("unused")
    private boolean debug = logger.isDebugEnabled();
    private boolean trace = logger.isTraceEnabled();

    private Sim    sim;
    private TimeInstant lastChangeoverTime = null;
    private boolean firstControl = true;

    protected Item         currentSetup;
    protected Machine      machine;
    protected boolean      hasDiscreteMaterial;
    protected PolicyParams policyParams;
    protected Clock        clock;
    @Getter protected IServiceLevelController serviceLevelController;


    @Override
    public void setUpPolicy(Sim sim) {
        this.sim = sim;
        this.machine = sim.getMachine();
        this.hasDiscreteMaterial = sim.hasDiscreteMaterial();
        this.policyParams = sim.getParams().getPolicyParams();
        this.clock = sim.getClock();
        if ( this.policyParams.isEnableServiceLevelController() ) {
            this.serviceLevelController = new ProportionalServiceLevelController(sim);
        } else {
            this.serviceLevelController = new InactiveServiceLevelController();
        }
    }

    public void updateControl(Sim sim) {

        this.currentSetup = machine.getSetup();
        this.serviceLevelController.updateSurplus();

        if ( this.firstControl ) {
            logger.debug("First control event for the policy. Acknowledging the new setup.");
            noteNewSetup();
            this.firstControl = false;
        }

        if (machine.isDown()) {
            logger.trace("Machine is down.");
            onFailure();
        }

        if (machine.isUp()) {

            if (!machine.isChangingSetups()) {

                if (isTimeToChangeOver()) {
                    logger.trace(String.format("The machine is ready to change setups at time %s", clock.getTime()));
                    // Inform implementations that the run finished
                    noteEndOfRun();
                    startChangeover(nextItem());
                } else {
                    logger.trace(String.format("The machine is in the middle of a production run at time %s", clock.getTime()));
                    ControlEvent nextControl = onReady();
                    sim.getMasterScheduler().addEvent(nextControl);
                }

            } else if (machine.isSetupComplete()) {
                logger.trace(String.format("The machine has finished its setup change at time %s."
                        + " Next control event will determine how much work to do", clock.getTime()));
                machine.setSprint();
                // Inform implementations that the setup is complete
                noteNewSetup();
                sim.getMasterScheduler().addEvent(new ControlEvent(sim.getTime()));

            } else {
                logger.debug(String.format("Nothing to do. Setup in progress and it is non-preemptive (time %s).", clock.getTime()));
                sim.getMasterScheduler().addEvent(new ControlEvent(machine.getNextSetupCompleteTime()));
            }
        }
    }

    /**
     * Called once the current run has completed, and before the changeover to the next setup.
     */
    protected void noteEndOfRun() {
        if ( logger.isDebugEnabled() ) {
            String.format("Finishing run of item %s", this.currentSetup);
        }
    }

    /**
     * Called once a new setup has been identified. Can be overridden to update internal logic of the implementations.
     */
    protected void noteNewSetup() {
        if ( logger.isDebugEnabled() ) {
            String.format("Registering a new changeover to item %s", this.currentSetup);
        }
        this.serviceLevelController.noteNewSetup(this.currentSetup);
    }

    /**
     * Override with whatever logic the policy implements when the machine is
     * down.
     * 
     * @param sim
     */
    protected void onFailure() {
        // Flush the control schedule
        sim.getMasterScheduler().dumpEvents();
    }

    /**
     * The machine is ready to produce, i.e., it is not under repair, and no
     * changeover is in progress or due at this time. Override with any commands
     * you want to execute during the run, and return the time when a new
     * control update should occur.
     * 
     * @return ControlEvent An event for when the next update should occur
     */
    protected abstract ControlEvent onReady();

    /**
     * Returns true if the machine should start changing over to some other
     * item, given by nextItem.
     * 
     * @return boolean
     */
    protected abstract boolean isTimeToChangeOver();

    /**
     * Implements the decision rule for selecting the next item if a changeover
     * is valid. If the state does not allow a changeover, returns
     * <code>null</code>.
     * 
     * @return
     */
    protected abstract Item nextItem();

    /**
     * A helper method to simplify the policy class. Adds a changeover event to
     * the given item to start now.
     * 
     * @param item
     * 
     */
    protected void startChangeover(@NonNull Item item) {
        if (trace) {
            logger.trace(String.format("Scheduling a changeover to the new item %s. Last changeover was at %s",
                    item, lastChangeoverTime));
        }
        if ( item.equals(this.machine.getSetup()) ) {
            logger.warn(String.format("Changing over to the same current setup %s!", item));
        }
        sim.getMasterScheduler().addEvent(new Changeover(sim.getTime(), item));
        lastChangeoverTime = clock.getTime();
    }

    protected SurplusCostLowerBound getMakeToOrderLowerBound() {
        return sim.getSurplusCostLowerBound();
    }

    protected double getSurplusTargetWithControl(final Item item) {
        return item.getSurplusTarget() + this.serviceLevelController.getControl(item);
    }

    protected double getSurplusDeviationWithControl(final Item item) {
        return item.getSurplusDeviation() + this.serviceLevelController.getControl(item);
    }

}
