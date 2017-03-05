package params;

import java.util.Iterator;

import com.google.common.collect.ImmutableList;

import lombok.Getter;
import lombok.ToString;
import lowerbounds.SurplusCostLowerBound;
import sequences.OptimalFCyclicSchedule;
import system.Item;

@ToString
@Getter
public class DerivedParams extends AbstractParams {

    protected double                 surplusCostLowerBound;
    protected ImmutableList<Double>  serviceLevels;
    protected ImmutableList<Double>  surplusCostLowerBoundIdealSurplusDeviations;
    protected ImmutableList<Double>  surplusCostLowerBoundIdealSetupFreq;
    protected String                 gallegoRecoveryPolicySequence;
    protected double                 gallegoRecoveryPolicyOptimalFCyclicCost;

    public DerivedParams(final Params params) {

        // Add the service levels
        ImmutableList.Builder<Double> serviceLevelsBuilder = ImmutableList.builder();
        for (int i = 0; i < params.getNumItems(); i++) {
            // The service level is b / (h + b) = 1 / (h/b + 1)
            double serviceLevel = 1
                    / ((params.getInventoryHoldingCosts().get(i) / params.getBacklogCosts().get(i)) + 1);
            serviceLevelsBuilder.add(serviceLevel);
        }
        serviceLevels = serviceLevelsBuilder.build();

    }

    public void setSurplusCostLowerBound(final SurplusCostLowerBound lb) {
        surplusCostLowerBound = lb.getLowerBound();
        ImmutableList.Builder<Double> deviationsListBuilder = ImmutableList.builder();
        ImmutableList.Builder<Double> freqListBuilder = ImmutableList.builder();
        for (int i = 0; i < lb.getNumItems(); i++) {
            deviationsListBuilder.add(lb.getIdealSurplusDeviation(i));
            freqListBuilder.add(lb.getIdealFrequency(i));
        }
        surplusCostLowerBoundIdealSurplusDeviations = deviationsListBuilder.build();
        surplusCostLowerBoundIdealSetupFreq = freqListBuilder.build();
    }

    public void setGallegoRecoveryPolicySequence(final OptimalFCyclicSchedule schedule) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        Iterator<Item> it = schedule.getSequence().iterator();
        while ( it.hasNext() ) {
            sb.append(it.next().getId());
            sb.append( it.hasNext() ? "," : "]");
        }
        this.gallegoRecoveryPolicySequence = sb.toString().trim();
        this.gallegoRecoveryPolicyOptimalFCyclicCost = schedule.getScheduleCost();
    }

}
