package params;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.google.common.collect.ImmutableList;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import policies.ClearTheLargestDeviationPolicy;
import policies.tuning.CMuComparator;
import policies.tuning.MakeToOrderBoundBasedLowerHedgingPointsComputationMethod;

@AllArgsConstructor(staticName = "of")
@ToString
@Getter
@Setter(AccessLevel.PACKAGE)
@Builder(toBuilder = true)
@JsonDeserialize( builder = PolicyParams.PolicyParamsBuilder.class )
public class PolicyParams extends AbstractParams {

    public static final String DEFAULT_PRIORITY_COMPARATOR = CMuComparator.class.getSimpleName();
    public static final String DEFAULT_LOWER_HEDGING_POINTS_COMPUTATION_METHOD = 
            MakeToOrderBoundBasedLowerHedgingPointsComputationMethod.class.getSimpleName();

    /**
     * Use {@link PolicyParams#builder()} for the same functionality.
     * @return
     */
    public static PolicyParamsBuilder builderWithDefaults() {
        return PolicyParams.builder();
    }

    @JsonProperty
    @Builder.Default
    protected String name = ClearTheLargestDeviationPolicy.class.getSimpleName();

    @JsonProperty
    @Builder.Default
    protected Optional<ImmutableList<Double>> userDefinedLowerHedgingPoints = Optional.empty();

    @JsonProperty
    @Builder.Default
    protected String lowerHedgingPointsComputationMethod = DEFAULT_LOWER_HEDGING_POINTS_COMPUTATION_METHOD;

    @JsonProperty
    @Builder.Default
    protected String priorityComparator = DEFAULT_PRIORITY_COMPARATOR;

    @JsonProperty
    @Builder.Default
    protected Optional<Boolean> userDefinedIsCruising = Optional.empty();

    @JsonProperty
    @Builder.Default
    protected Optional<ImmutableList<Integer>> userDefinedProductionSequence = Optional.empty();

    @JsonProperty
    @Builder.Default
    protected Optional<Integer> maxProductionSequenceLength = Optional.empty();

    /*
     * For the IdealDeviationAndFrequencyTrackingPolicy
     */
    @JsonProperty
    @Builder.Default
    protected double freqTrackingThreshold = 1.0;

    @JsonProperty
    @Builder.Default
    protected double learningRate = 0.5;

    @JsonProperty
    @Builder.Default
    protected double deviationTrackingBias = 0.5;

    @JsonProperty
    @Builder.Default
    protected double serviceLevelControllerInitialLearningRate = 0.25;

    @JsonProperty
    @Builder.Default
    protected double serviceLevelControllerLearningRateDecayFactor = 0.95;

    @JsonProperty
    @Builder.Default
    protected int serviceLevelControllerChangeoversPerCycle = 100;

    @JsonProperty
    @Builder.Default
    protected double serviceLevelControllerPropGain = 5;

    @Deprecated
    @JsonProperty
    protected ImmutableList<Double> lowerHedgingPoints;

    @Deprecated
    @JsonProperty
    protected boolean isCruising;

    @JsonPOJOBuilder(buildMethodName = "build", withPrefix = "")
    public static class PolicyParamsBuilder {
    }

}
