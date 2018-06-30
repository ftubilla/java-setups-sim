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

@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@ToString
@Getter
@Setter(AccessLevel.PACKAGE)
@Builder(toBuilder = true)
public class Params extends AbstractParams {

    // Note: another pattern to maintain the default values when deserializing with missing inputs,
    // is to use JsonDeserializer(builder = ParamsBuilder.class) and then to annotate the builder with
    // @JsonPOJOBuilder(builderMethodName = "build", withPrefix = "")

    /**
     * Use {@link Params#builder} instead for the same functionality. 
     * @return
     */
    public static ParamsBuilder builderWithDefaults() {
        return Params.builder();
    }

    @JsonProperty
    @Builder.Default
    protected int numItems = 3;

    @JsonProperty
    @NonNull
    @Builder.Default
    protected ImmutableList<Double> demandRates = ImmutableList.of(1.0, 1.0, 1.0);

    @JsonProperty
    @NonNull
    @Builder.Default
    protected ImmutableList<Double> productionRates = ImmutableList.of(5.0, 5.0, 5.0);

    @JsonProperty
    @NonNull
    @Builder.Default
    protected ImmutableList<Double> setupTimes = ImmutableList.of(10.0, 10.0, 10.0); 

    @JsonProperty
    @NonNull
    @Builder.Default
    protected ImmutableList<Double> surplusTargets = ImmutableList.of(0.0, 0.0, 0.0);

    @JsonProperty
    @NonNull
    @Builder.Default
    protected ImmutableList<Double> inventoryHoldingCosts = ImmutableList.of(1.0, 1.0, 1.0);

    @JsonProperty
    @NonNull
    @Builder.Default
    protected ImmutableList<Double> backlogCosts = ImmutableList.of(1.0, 1.0, 1.0);

    @JsonProperty
    @Builder.Default
    protected double meanTimeToFail = 1.0;

    @JsonProperty
    @Builder.Default
    protected double meanTimeToRepair = 0.0;

    @JsonProperty
    @Builder.Default
    protected double finalTime = 100;

    @JsonProperty
    @Builder.Default
    protected double metricsStartTime = 0;

    @JsonProperty
    @Builder.Default
    protected boolean recordHighFreq = false;

    @JsonProperty
    protected long seed;

    @JsonProperty
    @Builder.Default
    protected int initialSetup = 0;

    @JsonProperty
    @Builder.Default
    protected Double convergenceTolerance = 1e-4;

    @JsonProperty
    @Builder.Default
    protected ImmutableList<Double> initialDemand = ImmutableList.of(100.0, 100.0, 100.0);

    @JsonProperty
    @Builder.Default
   protected DemandProcessParams demandProcessParams = DemandProcessParams.DEFAULT;

    @JsonProperty
    @Builder.Default
    protected ProductionProcessParams productionProcessParams = ProductionProcessParams.DEFAULT;

    @JsonProperty
    @NonNull
    @Builder.Default
    protected PolicyParams policyParams = PolicyParams.builder().build();

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
