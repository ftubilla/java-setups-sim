package policies;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import discreteEvent.ControlEvent;
import lombok.Getter;
import lombok.extern.apachecommons.CommonsLog;
import policies.GRPControlCycle.GRPRunInfo;
import policies.tuning.GallegoRecoveryPolicyControlMatrixCalculator;
import sequences.OptimalFCyclicSchedule;
import sequences.OptimalSequenceFinder;
import sequences.ProductionSequence;
import sim.Sim;
import sim.TimeInstant;
import system.Item;

@CommonsLog
public class GallegoRecoveryPolicy extends AbstractPolicy {

    public static final double ARME_TOLERANCE = 1e-6;
    public static final double NON_CRUISING_CHECK_TOL = 1e-4;

    // Problem-specific parameters
    private int sequenceLength;
    private double[] sprintingTimeTarget;                // Corresponds to the vector t in the paper
    private Map<Item, Double> initialSurplusTarget;      // Corresponds to the vector w in the paper
    private double[][] gainMatrix;                       // Matrix G
    @Getter private OptimalFCyclicSchedule schedule;

    // Variables regarding the current control cycle
    private GRPControlCycle controlCycle;
    private GRPRunInfo currentRun;
    private TimeInstant currentRunStartTime;

    @Override
    public void setUpPolicy(final Sim sim) {

        super.setUpPolicy(sim);

        // Get the desired sequence
        ProductionSequence sequence = null;
        Optional<ImmutableList<Integer>> sequenceOpt = this.policyParams.getUserDefinedProductionSequence();
        if ( sequenceOpt.isPresent() ) {
            log.debug("Reading user-defined production sequence");
            List<Item> itemSequence = StreamSupport.stream( sequenceOpt.get().spliterator(), false)
                .map( i -> this.machine.getItemById(i) ).collect(Collectors.toList());
            sequence = new ProductionSequence(itemSequence);
        } else {
            Optional<Integer> maxLength = this.policyParams.getMaxProductionSequenceLength();
            if ( !maxLength.isPresent() ) {
                throw new RuntimeException("No max-sequence length or user-defined sequence given!");
            }
            Collection<Item> items = StreamSupport.stream(machine.spliterator(), false).collect(Collectors.toList());
            OptimalSequenceFinder sequenceFinder = new OptimalSequenceFinder(items, sim.getParams().getMachineEfficiency());
            try {
                OptimalFCyclicSchedule optimalSchedule = sequenceFinder.find(maxLength.get());
                sequence = optimalSchedule.getSequence();
            } catch (Exception e) {
                throw new RuntimeException("Could not compute the optimal f-cyclic schedule", e);
            }
        }

        // Compute the f-cyclic optimal schedule
        log.debug(String.format("Setting up GRP with sequence %s", sequence));
        this.sequenceLength = sequence.getSize();
        this.schedule = new OptimalFCyclicSchedule(sequence, sim.getParams().getMachineEfficiency());
        try {
            this.schedule.compute();
        } catch (Exception e) {
            throw new RuntimeException("Could not solve for the optimal f-cyclic schedule", e);
        }
        sim.getDerivedParams().setGallegoRecoveryPolicySequence(this.schedule);
        log.debug(String.format("Computed optimal f-cyclic schedule with cost %.2f", this.schedule.getScheduleCost()));

        // Ensure that the schedule is non-cruising
        for ( int n = 0; n < this.sequenceLength; n++ ) {
            if ( this.schedule.getOptimalCruisingTime(n) > NON_CRUISING_CHECK_TOL ) {
                throw new RuntimeException("GRP only works with 0 cruising time!");
            }
        }

        // Get the initial surplus and target sprinting times
        this.sprintingTimeTarget = new double[this.sequenceLength];
        for ( int n = 0; n < this.sequenceLength; n++ ) {
            this.sprintingTimeTarget[n] = this.schedule.getOptimalSprintingTimeWithBacklog(n) +
                    this.schedule.getOptimalSprintingTimeWithInventory(n);
            log.debug(String.format("Position %d has a sprinting target time of %.2f", n, this.sprintingTimeTarget[n]));
        }
        this.initialSurplusTarget = new HashMap<>();
        for ( Item item : sim.getMachine() ) {
            this.initialSurplusTarget.put(item,
                    this.schedule.getSurplusPriorToFirstSetup(item));
            log.debug(String.format("%s has a start-of-cycle surplus target of %.2f", item, this.initialSurplusTarget.get(item)));
        }

        // Calculate the G matrix
        GallegoRecoveryPolicyControlMatrixCalculator gainCalculator = 
                new GallegoRecoveryPolicyControlMatrixCalculator(sim.getParams());
        try {
            this.gainMatrix = gainCalculator.compute(sequence, false, ARME_TOLERANCE);
        } catch (Exception e) {
            throw new RuntimeException("Could not solve the ARME to obtain the gain matrix", e);
        }

    }

    @Override
    public boolean isTargetBased() {
        return false;
    }

    @Override
    protected ControlEvent onReady() {
        if ( this.currentRunStartTime == null ) {
            this.currentRunStartTime = this.clock.getTime();
            log.trace(String.format("Starting run %s at time %.2f", this.currentRun, this.currentRunStartTime.doubleValue()));
        }
        double remainingTime = this.getTimeRemainingCurrentRun();
        log.trace(String.format("Machine should continue sprinting for %.2f more time units", remainingTime));
        return new ControlEvent(this.clock.getTime().add(remainingTime));
    }

    @Override
    protected boolean isTimeToChangeOver() {
        double timeTolerance = Sim.SURPLUS_TOLERANCE / this.machine.getSetup().getDemandRate();
        double remainingTime = this.getTimeRemainingCurrentRun();
        log.trace(String.format("Checking if it's time for a changeover: remaining run time %6.3e and tolerance %1.2e)",
               remainingTime, timeTolerance));
        return remainingTime <= timeTolerance;
    }

    @Override
    protected Item nextItem() {
        if ( this.controlCycle == null || !this.controlCycle.hasNext() ) {
            this.controlCycle = new GRPControlCycle(this.machine, this.getSchedule().getSequence(), this.initialSurplusTarget, this.sprintingTimeTarget, this.gainMatrix);
            log.trace(String.format("Computing a new control cycle:%n%s", this.controlCycle));
        }
        this.currentRun = this.controlCycle.next();
        this.currentRunStartTime = null;
        Item nextItem = this.currentRun.getItem();
        if ( this.currentRun.getRunDuration() < 0 ) {
            log.trace(String.format("The duration for %s is negative, so it will be capped at 0", nextItem));
        }
        return nextItem;
    }

    @VisibleForTesting
    protected double getTimeRemainingCurrentRun() {
        if ( this.currentRun == null ) {
            return 0.0;
        }
        // If the current run start time is null, the run has not started so elapsed time is 0
        double elapsedTime = this.currentRunStartTime == null ? 0 :
            this.clock.getTime().subtract(this.currentRunStartTime).doubleValue();
        return Math.max(0, this.currentRun.getRunDuration() - elapsedTime );
    }

    /**
     * Returns the <i>target</i> sprinting time of the current run in the sequence.
     * @return
     */
    public Double getSprintingTimeTargetCurrentRun() {
        if ( this.currentRun == null ) {
            return null;
        } else {
            return this.currentRun.getRunDuration();
        }
    }

}
