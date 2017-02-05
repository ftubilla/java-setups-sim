package policies;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.common.collect.ImmutableList;

import discreteEvent.ControlEvent;
import policies.tuning.GallegoRecoveryPolicyControlMatrixCalculator;
import sequences.OptimalFCyclicSchedule;
import sequences.ProductionSequence;
import sim.Sim;
import system.Item;

public class GallegoRecoveryPolicy extends AbstractPolicy {

    public static final double ARME_TOLERANCE = 1e-6;
    public static final double NON_CRUISING_CHECK_TOL = 1e-4;
    
    private OptimalFCyclicSchedule schedule;
    private Integer currentSequencePosition;
    private Double timeRemainingCurrentRun;
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
        this.sequenceLength = sequence.getSize();
        this.schedule = new OptimalFCyclicSchedule(sequence, sim.getParams().getMachineEfficiency());
        try {
            this.schedule.compute();
        } catch (Exception e) {
            throw new RuntimeException("Could not solve for the optimal f-cyclic schedule", e);
        }

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
        }
        this.initialSurplusTarget = new HashMap<>();
        for ( Item item : sim.getMachine() ) {
            this.initialSurplusTarget.put(item,
                    this.schedule.getSurplusPriorToFirstSetup(item));
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected boolean isTimeToChangeOver() {
        return this.currentSequencePosition == null ||
                    ( this.timeRemainingCurrentRun != null && this.timeRemainingCurrentRun <= 0 );
    }

    @Override
    protected Item nextItem() {
        this.schedule.getSequence().getNextPosition(this.currentSequencePosition);
        return null;
    }
    
    

}
