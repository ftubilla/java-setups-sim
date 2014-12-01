package params;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

@NoArgsConstructor
@ToString
@Getter
@Setter(AccessLevel.PACKAGE)
public class PolicyParams extends AbstractParams {
	
	public static final String DEFAULT_PRIORITY_COMPARATOR = "CMuComparatorWithTiesById";
	public static final String DEFAULT_LOWER_HEDGING_POINTS_COMPUTATION_METHOD = "MakeToOrderBoundBasedLowerHedgingPointsComputationMethod";
	
	@JsonProperty
	protected String name;
	
	@JsonProperty
	protected Optional<ImmutableList<Double>> userDefinedLowerHedgingPoints = Optional.absent();
		
	@JsonProperty
	protected String lowerHedgingPointsComputationMethod = DEFAULT_LOWER_HEDGING_POINTS_COMPUTATION_METHOD;

	@JsonProperty 
	protected String priorityComparator = DEFAULT_PRIORITY_COMPARATOR;
	
	@JsonProperty
	protected Optional<Boolean> userDefinedIsCruising = Optional.absent();
	
	@Deprecated
	@JsonProperty
	protected ImmutableList<Double> lowerHedgingPoints;
	
	@Deprecated
	@JsonProperty
	protected boolean isCruising;

}


