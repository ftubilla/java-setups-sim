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
public class Params {
	
	@JsonProperty
	private int numItems;

	@JsonProperty
	@NonNull
	private ImmutableList<Double> demandRates;
	
	@JsonProperty
	@NonNull
	private ImmutableList<Double> productionRates;

	@JsonProperty
	@NonNull
	private ImmutableList<Double> setupTimes;
	
	@JsonProperty
	@NonNull
	private ImmutableList<Double> surplusTargets;

	@JsonProperty
	@NonNull
	private ImmutableList<Double> inventoryHoldingCosts;

	@JsonProperty
	@NonNull
	private ImmutableList<Double> backlogCosts;
	
	@JsonProperty
	private double meanTimeToFail = 1.0;
	
	@JsonProperty
	private double meanTimeToRepair = 0.0;
	
	@JsonProperty
	private double finalTime;

	@JsonProperty
	private double metricsStartTime;
	
	@JsonProperty
	private boolean recordHighFreq = false;
	
	@JsonProperty
	private long seed;
	
	@JsonProperty
	private int initialSetup;
	
	@JsonProperty
	private ImmutableList<Double> initialDemand;
	
	@JsonProperty
	@NonNull
	private DemandProcessParams demandProcessParams;
	
	@JsonProperty
	@NonNull
	private ProductionProcessParams productionProcessParams;
	
	@JsonProperty
	@NonNull
	private PolicyParams policyParams;
	
}
