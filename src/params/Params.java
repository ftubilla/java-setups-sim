/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * © 2012 Fernando Tubilla. All rights reserved.
 */

package params;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

@NoArgsConstructor
@ToString
@Getter
@Setter(AccessLevel.PACKAGE)
public class Params extends AbstractParams {
	
	@JsonProperty
	protected int numItems;

	@JsonProperty
	@NonNull
	protected ImmutableList<Double> demandRates;
	
	@JsonProperty
	@NonNull
	protected ImmutableList<Double> productionRates;

	@JsonProperty
	@NonNull
	protected ImmutableList<Double> setupTimes;
	
	@JsonProperty
	@NonNull
	protected ImmutableList<Double> surplusTargets;

	@JsonProperty
	@NonNull
	protected ImmutableList<Double> inventoryHoldingCosts;

	@JsonProperty
	@NonNull
	protected ImmutableList<Double> backlogCosts;
	
	@JsonProperty
	protected double meanTimeToFail = 1.0;
	
	@JsonProperty
	protected double meanTimeToRepair = 0.0;
	
	@JsonProperty
	protected double finalTime;

	@JsonProperty
	protected double metricsStartTime;
	
	@JsonProperty
	protected boolean recordHighFreq = false;
	
	@JsonProperty
	protected long seed;
	
	@JsonProperty
	protected int initialSetup;
	
	@JsonProperty
	protected ImmutableList<Double> initialDemand;
	
	@JsonProperty
	@NonNull
	protected DemandProcessParams demandProcessParams;
	
	@JsonProperty
	@NonNull
	protected ProductionProcessParams productionProcessParams;
	
	@JsonProperty
	@NonNull
	protected PolicyParams policyParams;
	
}
