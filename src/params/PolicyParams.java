package params;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import system.Item;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

@NoArgsConstructor
@ToString
@Getter
@Setter(AccessLevel.PACKAGE)
public class PolicyParams extends AbstractParams {

	@JsonProperty
	protected String name;
	
	@JsonProperty
	protected ImmutableList<Double> lowerHedgingPoints;
	
	//Hedging Zone Policy Params
	@JsonProperty 
	protected String priorityComparator="hzp.CMuComparatorWithTiesById";
	
	@JsonProperty
	protected boolean isCruising=false;

	public double getHedgingThresholdDifference(Item item){
		return item.getSurplusTarget()-lowerHedgingPoints.get(item.getId());
	}

	public double getLowerHedgingPoint(Item item) {
		return lowerHedgingPoints.get(item.getId());
	}

}


