/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * © 2012 Fernando Tubilla. All rights reserved.
 */

package sim;

import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

import policies.PolicyParams;
import processes.demand.DemandProcessParams;
import processes.production.ProductionProcessParams;

public class Params {

	@JsonProperty private int numItems;
	@JsonProperty private List<Double> demandRates;
	@JsonProperty private List<Double> productionRates;
	@JsonProperty private List<Double> setupTimes;
	@JsonProperty private List<Double> surplusTargets;
	@JsonProperty private List<Double> inventoryHoldingCosts;
	@JsonProperty private List<Double> backlogCosts;
	@JsonProperty private double meanTimeToFail = 1.0;
	@JsonProperty private double meanTimeToRepair = 0.0;
	@JsonProperty private double finalTime;
	@JsonProperty private double metricsStartTime;
	@JsonProperty private boolean recordHighFreq = false;
	@JsonProperty private long seed;
	@JsonProperty private DemandProcessParams demandProcessParams;
	@JsonProperty private ProductionProcessParams productionProcessParams;
	@JsonProperty private PolicyParams policyParams;


	private int initialSetup=0;
	private List<Double> initialDemand;
	private boolean isLocked=false;
	
	public Params(){
		//Set up some defaults here
		seed = System.currentTimeMillis();
	}
	
	public void lock(){
		isLocked=true;
		policyParams.lock();
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

	public long getSeed(){
		return seed;
	}
	
	public DemandProcessParams getDemandProcessParams(){
		return demandProcessParams;
	}
	
	public ProductionProcessParams getProductionProcessParams(){
		return productionProcessParams;
	}
	
	public PolicyParams getPolicyParams(){
		return policyParams;
	}


	public List<Double> getInventoryHoldingCosts() {
		return inventoryHoldingCosts;
	}


	public List<Double> getBacklogCosts() {
		return backlogCosts;
	}

	//Setters. Don't forget to check if the params are locked!---------
	
	public void setNumItems(int numItems) {
		assert !isLocked : "The params are locked!";
		this.numItems = numItems;
	}

	public void setDemandRates(List<Double> demandRates) {
		assert !isLocked : "The params are locked!";
		this.demandRates = demandRates;
	}

	public void setProductionRates(List<Double> productionRates) {
		assert !isLocked : "The params are locked!";
		this.productionRates = productionRates;
	}

	public void setSetupTimes(List<Double> setupTimes) {
		assert !isLocked : "The params are locked!";
		this.setupTimes = setupTimes;
	}

	public void setSurplusTargets(List<Double> surplusTargets) {
		assert !isLocked : "The params are locked!";
		this.surplusTargets = surplusTargets;
	}

	public void setInventoryHoldingCosts(List<Double> inventoryHoldingCosts) {
		assert !isLocked : "The params are locked!";
		this.inventoryHoldingCosts = inventoryHoldingCosts;
	}

	public void setBacklogCosts(List<Double> backlogCosts) {
		assert !isLocked : "The params are locked!";
		this.backlogCosts = backlogCosts;
	}

	public void setMeanTimeToFail(double meanTimeToFail) {
		assert !isLocked : "The params are locked!";
		this.meanTimeToFail = meanTimeToFail;
	}

	public void setMeanTimeToRepair(double meanTimeToRepair) {
		assert !isLocked : "The params are locked!";
		this.meanTimeToRepair = meanTimeToRepair;
	}

	public void setFinalTime(double finalTime) {
		assert !isLocked : "The params are locked!";
		this.finalTime = finalTime;
	}

	public void setMetricsStartTime(double metricsStartTime) {
		assert !isLocked : "The params are locked!";
		this.metricsStartTime = metricsStartTime;
	}

	public void setSeed(long seed) {
		assert !isLocked : "The params are locked!";
		this.seed = seed;
	}

	public void setDemandProcessParams(DemandProcessParams demandProcessParams) {
		assert !isLocked : "The params are locked!";
		this.demandProcessParams = demandProcessParams;
	}

	public void setProductionProcessParams(ProductionProcessParams productionProcessParams) {
		assert !isLocked : "The params are locked!";
		this.productionProcessParams = productionProcessParams;
	}

	public void setPolicyParams(PolicyParams policyParams) {
		assert !isLocked : "The params are locked!";
		this.policyParams = policyParams;
	}

	public void setInitialSetup(int initialSetup) {
		assert !isLocked : "The params are locked!";
		this.initialSetup = initialSetup;
	}

	public void setInitialDemand(List<Double> initialDemand) {
		assert !isLocked : "The params are locked!";
		this.initialDemand = initialDemand;
	}

	public boolean isRecordHighFreqEnabled() {
		return recordHighFreq;
	}

	public void setRecordHighFreq(boolean recordHighFreq) {
		this.recordHighFreq = recordHighFreq;
	}
	
}
