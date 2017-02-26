/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * � 2012 Fernando Tubilla. All rights reserved.
 */

package params;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter(AccessLevel.PACKAGE)
@Builder(toBuilder = true)
public class Params extends AbstractParams {

    public static final double DEFAULT_CONVERGENCE_TOLERANCE = 1e-6;
    
    public static ParamsBuilder builderWithDefaults() {
        Params params = new Params();
        return params.toBuilder();
    }
    
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
    protected Double convergenceTolerance;

    public double getConvergenceTolerance() {
        return this.convergenceTolerance == null ? DEFAULT_CONVERGENCE_TOLERANCE : this.convergenceTolerance;
    }

    @JsonProperty
    protected ImmutableList<Double> initialDemand;

    @JsonProperty
   protected DemandProcessParams demandProcessParams = DemandProcessParams.DEFAULT;

    @JsonProperty
    protected ProductionProcessParams productionProcessParams = ProductionProcessParams.DEFAULT;

    @JsonProperty
    @NonNull
    protected PolicyParams policyParams = new PolicyParams();

    protected String file;

    @JsonIgnore
    public double getMachineEfficiency() {
        if (Double.isInfinite(this.meanTimeToFail) || this.meanTimeToRepair == 0) {
            return 1.0;
        } else {
            return 1 / (1 + this.meanTimeToRepair / this.meanTimeToFail);
        }
    }
}
