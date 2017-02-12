package policies;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import discreteEvent.ControlEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.apachecommons.CommonsLog;
import policies.tuning.GallegoRecoveryPolicyControlMatrixCalculator;
import sequences.OptimalFCyclicSchedule;
import sequences.ProductionSequence;
import sim.Sim;
import system.Item;

@CommonsLog
public class GallegoRecoveryPolicy extends AbstractPolicy {

    public static final double ARME_TOLERANCE = 1e-6;
    public static final double NON_CRUISING_CHECK_TOL = 1e-4;
    
    @Getter private OptimalFCyclicSchedule schedule;
    private Integer currentSequencePosition;
    private Double startTimeProductionCurrentRun;
    @VisibleForTesting @Getter(AccessLevel.PROTECTED) private double sprintingTimeTargetCurrentRun;
    private int sequenceLength;
    private double[] sprintingTimeTarget;                // Corresponds to the vector t in the paper
    private Map<Item, Double> initialSurplusTarget;      // Corresponds to the vector w in the paper
    private double[] sprintingTimeCorrection;            // Corresponds to the vector v in the paper
    private double[][] gainMatrix;                       // Matrix G

    @Override
    public void setUpPolicy(final Sim sim) {

        super.setUpPolicy(sim);

        // Get the desired sequence and compute the optimal f-cyclic schedule
        Optional<ImmutableList<Integer>> sequenceOpt = this.policyParams.getUserDefinedProductionSequence();
        if ( !sequenceOpt.isPresent() ) {
            throw new RuntimeException("Currently, GRP needs a user defined sequence");
        }
        List<Item> itemSequence = StreamSupport.stream( sequenceOpt.get().spliterator(), false)
                .map( i -> this.machine.getItemById(i) ).collect(Collectors.toList());
        ProductionSequence sequence = new ProductionSequence(itemSequence);
        log.debug(String.format("Setting up GRP with sequence %s", sequence));
        this.sequenceLength = sequence.getSize();
        this.schedule = new OptimalFCyclicSchedule(sequence, sim.getParams().getMachineEfficiency());
        try {
            this.schedule.compute();
        } catch (Exception e) {
            throw new RuntimeException("Could not solve for the optimal f-cyclic schedule", e);
        }
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

        // Initialize the correction vector
        this.sprintingTimeCorrection = new double[this.sequenceLength];
        
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
        if ( this.startTimeProductionCurrentRun == null ) {
            log.trace(String.format("First call to onReady at current sequence position %d. Setting start time of run to %.2f",
                    this.currentSequencePosition, this.clock.getTime()));
            this.startTimeProductionCurrentRun = this.clock.getTime();
        }
        double remainingTime = this.getTimeRemainingCurrentRun();
        log.trace(String.format("Machine should continue sprinting for %.2f more time units", remainingTime));
        return new ControlEvent(this.clock.getTime() + remainingTime);
    }

    @Override
    protected boolean isTimeToChangeOver() {
        double timeTolerance = Sim.SURPLUS_TOLERANCE / this.machine.getSetup().getDemandRate();
        double remainingTime = this.getTimeRemainingCurrentRun();
        log.trace(String.format("Checking if it's time for a changeover, from current position %d and remaining run time %6.3e and tolerance %1.2e)",
                this.currentSequencePosition, remainingTime, timeTolerance));
        return this.currentSequencePosition == null || remainingTime <= timeTolerance;
    }

    @Override
    protected Item nextItem() {

        if ( this.currentSequencePosition == null ) {
            log.trace("There is no current sequence position, setting it to the first position (i.e., 0)");
            this.currentSequencePosition = 0;
            this.updateControlCycle();
        } else {
            // Find the next position within the sequence with a positive sprinting time. If we have to circle back, update
            // the control 
            Integer newPosition = null;
            for ( int i = 1; i <= this.sequenceLength; i++ ) {
                int candidateNewPosition = ( this.currentSequencePosition + i ) % this.sequenceLength;
                if ( candidateNewPosition == 0 ) {
                    log.trace("Position 0 could be next. Updating the cycle's control.");
                    this.updateControlCycle();
                }
                double correctedSprintingTime = this.sprintingTimeTarget[candidateNewPosition] + this.sprintingTimeCorrection[candidateNewPosition];
                if ( correctedSprintingTime > 0 ) {
                    newPosition = candidateNewPosition;
                    break;
                } else {
                    log.debug(String.format("Skipping position %d because it has a nonpositive sprinting time %.2f",
                            candidateNewPosition, correctedSprintingTime));
                }
            }
            if ( newPosition == null ) {
                throw new RuntimeException("All sequence positions had a negative production time!");
            }
            log.trace(String.format("Changing current sequence position from %d to %d", this.currentSequencePosition, newPosition));
            this.currentSequencePosition = newPosition;
        }

        this.sprintingTimeTargetCurrentRun = this.sprintingTimeTarget[this.currentSequencePosition] + 
                this.sprintingTimeCorrection[this.currentSequencePosition];
        log.trace(String.format("The production target time for the current position is set to %.2f", this.sprintingTimeTargetCurrentRun));
        if ( this.sprintingTimeTargetCurrentRun <= 0 ) {
            throw new RuntimeException(String.format("The target sprinting time is non-positive (%.2f)", this.sprintingTimeTargetCurrentRun));
        }
        // Reset the start time of the current run
        this.startTimeProductionCurrentRun = null;
        return this.schedule.getSequence().getItemAtPosition(this.currentSequencePosition);
    }

    @VisibleForTesting
    protected double getTimeRemainingCurrentRun() {
        if ( this.currentSequencePosition == null ) {
            return 0.0;
        }
        double elapsedTime = this.startTimeProductionCurrentRun == null ? 0.0 : this.clock.getTime() - this.startTimeProductionCurrentRun;
        return Math.max(0, this.sprintingTimeTargetCurrentRun - elapsedTime );
    }

    @VisibleForTesting
    protected void updateControlCycle() {
        log.trace("New control cycle. Updating correction vector.");
        int n = this.machine.getNumItems();
        double[] error = new double[n];
        for ( int j = 0; j < n; j++ ) {
            Item item = this.machine.getItemById(j);
            error[j] = this.initialSurplusTarget.get(item) - item.getSurplus();
            log.trace(String.format("%s has a surplus error of %.2f (initial surplus target %.2f - current surplus %.2f)",
                    item, error[j], this.initialSurplusTarget.get(item), item.getSurplus()));
        }
        for ( int i = 0; i < this.sequenceLength; i++ ) {
            double correction = 0.0;
            for ( int j = 0; j < n; j++ ) {
                correction += this.gainMatrix[i][j] * error[j];
            }
            log.trace(String.format("The production time correction at position %d is %.2f. Updated sprinting time = %.2f",
                    i, correction, this.sprintingTimeTarget[i] + correction));
            this.sprintingTimeCorrection[i] = correction;
        }
    }

}
