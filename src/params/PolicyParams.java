package params;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import policies.ClearTheLargestDeviationPolicy;
import policies.tuning.CMuComparator;
import policies.tuning.MakeToOrderBoundBasedLowerHedgingPointsComputationMethod;

@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@ToString
@Getter
@Setter(AccessLevel.PACKAGE)
@Builder(toBuilder = true)
public class PolicyParams extends AbstractParams {

    public static final String DEFAULT_PRIORITY_COMPARATOR = CMuComparator.class.getSimpleName();
    public static final String DEFAULT_LOWER_HEDGING_POINTS_COMPUTATION_METHOD = 
            MakeToOrderBoundBasedLowerHedgingPointsComputationMethod.class.getSimpleName();

    public static PolicyParamsBuilder builderWithDefaults() {
        PolicyParams params = new PolicyParams();
        return params.toBuilder();
    }

    @JsonProperty
    protected String name = ClearTheLargestDeviationPolicy.class.getSimpleName();

    @JsonProperty
    protected Optional<ImmutableList<Double>> userDefinedLowerHedgingPoints = Optional.empty();

    @JsonProperty
    protected String lowerHedgingPointsComputationMethod = DEFAULT_LOWER_HEDGING_POINTS_COMPUTATION_METHOD;

    @JsonProperty
    protected String priorityComparator = DEFAULT_PRIORITY_COMPARATOR;

    @JsonProperty
    protected Optional<Boolean> userDefinedIsCruising = Optional.empty();

    @JsonProperty
    protected Optional<ImmutableList<Integer>> userDefinedProductionSequence = Optional.empty();

    @JsonProperty
    protected Optional<Integer> maxProductionSequenceLength = Optional.empty();

    /*
     * For the IdealDeviationAndFrequencyTrackingPolicy
     */
    @JsonProperty
    protected double freqTrackingThreshold = 1.0;

    @JsonProperty
    protected double learningRate = 0.5;

    @JsonProperty
    protected double deviationTrackingBias = 0.5;

    @Deprecated
    @JsonProperty
    protected ImmutableList<Double> lowerHedgingPoints;

    @Deprecated
    @JsonProperty
    protected boolean isCruising;

}
