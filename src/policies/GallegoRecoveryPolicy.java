package policies;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.google.common.collect.ImmutableList;

import discreteEvent.ControlEvent;
import sequences.OptimalFCyclicSchedule;
import sequences.ProductionSequence;
import sim.Sim;
import system.Item;

public class GallegoRecoveryPolicy extends AbstractPolicy {

    private OptimalFCyclicSchedule schedule;
    
    @Override
    public void setUpPolicy(final Sim sim) {
        super.setUpPolicy(sim);
        Optional<ImmutableList<Integer>> sequenceOpt = this.policyParams.getUserDefinedProductionSequence();
        if ( !sequenceOpt.isPresent() ) {
            throw new RuntimeException("Right now GRP needs a user defined sequence");
        }
        List<Item> itemSequence = StreamSupport.stream( sequenceOpt.get().spliterator(), false)
                .map( i -> this.machine.getItemById(i) ).collect(Collectors.toList());
        ProductionSequence sequence = new ProductionSequence(itemSequence);
        this.schedule = new OptimalFCyclicSchedule(sequence, sim.getParams().getMachineEfficiency());
        try {
            this.schedule.compute();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Could not solve for the optimal f-cyclic schedule");
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
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected Item nextItem() {
        // TODO Auto-generated method stub
        return null;
    }

}
