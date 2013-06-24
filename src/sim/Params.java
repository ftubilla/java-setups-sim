/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * © 2012 Fernando Tubilla. All rights reserved.
 */

package sim;

import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

public class Params {

	@JsonProperty private int numItems;
	@JsonProperty private List<Double> demandRates;
	@JsonProperty private List<Double> productionRates;
	@JsonProperty private List<Double> setupTimes;
	@JsonProperty private List<Double> surplusTargets;
	@JsonProperty private double meanTimeToFail = 1.0;
	@JsonProperty private double meanTimeToRepair = 0.0;
	@JsonProperty private double finalTime;
	@JsonProperty private double metricsStartTime;
	@JsonProperty private long seedFailuresGenerator;
	@JsonProperty private long seedRepairsGenerator;


	private int initialSetup=0;
	private List<Double> initialDemand;
	
	public Params(){
		//Set up some defaults here
		seedFailuresGenerator = System.currentTimeMillis();
		seedRepairsGenerator = System.currentTimeMillis()+1;
	}
	
	
	public int getInitialSetup() {
		return initialSetup;
	}

	public List<Double> getProductionRates() {
		return productionRates;
	}

	public List<Double> getSetupTimes() {
		return setupTimes;
	}


	public double getMeanTimeToFail() {
		return meanTimeToFail;
	}


	public double getMeanTimeToRepair() {
		return meanTimeToRepair;
	}


	public int getNumItems() {
		return numItems;
	}


	public List<Double> getDemandRates() {
		return demandRates;
	}

	public double getFinalTime() {
		return finalTime;
	}

	public List<Double> getInitialDemand() {
		return initialDemand;
	}

	public List<Double> getSurplusTargets() {
		return surplusTargets;
	}
	
	public double getMetricsStartTime() {
		return metricsStartTime;
	}

	public long getSeedFailuresGenerator() {
		return seedFailuresGenerator;
	}

	public long getSeedRepairsGenerator() {
		return seedRepairsGenerator;
	}
	
}
