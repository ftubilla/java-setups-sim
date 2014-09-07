/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * © 2012 Fernando Tubilla. All rights reserved.
 */

package system;

import org.apache.log4j.Logger;

import sim.Params;
import sim.Sim;

/**
 * A part or product type that can be produced by the machine.
 * 
 * @author ftubilla
 * 
 */
public class Item {

	private static Logger logger = Logger.getLogger(Item.class);

	// Parameters
	private double demandRate;
	private double productionRate;
	private double setupTime;
	private double inventoryCostRate;
	private double backlogCostRate;
	private double surplusTarget;
	private int id;

	// Variables
	private double cumulativeProduction;
	private double cumulativeDemand;
	private double surplus;
	private double inventory;
	private double backlog;
	private boolean isUnderProduction=false;	

	
	public Item(int id, Params params) {
		this.id = id;
		demandRate = params.getDemandRates().get(id);
		productionRate = params.getProductionRates().get(id);
		setupTime = params.getSetupTimes().get(id);
		setCumulativeDemand(params.getInitialDemand().get(id));
		surplusTarget = params.getSurplusTargets().get(id);
		inventoryCostRate = params.getInventoryHoldingCosts().get(id);
		backlogCostRate = params.getBacklogCosts().get(id);
		logger.debug("Created Item " + id + " with demand rate: " + demandRate
				+ " production rate: " + productionRate + " setup time: "
				+ setupTime + " surplus target: " + surplusTarget + " cost rates: inventory " + inventoryCostRate + 
				" backlog " + backlogCostRate);
	}

	public String toString() {
		return "Item:" + id;
	}

	public double getCumulativeProduction() {
		return cumulativeProduction;
	}

	public void setCumulativeProduction(double cumulativeProduction) {
		logger.debug("Setting the cum. production of item " + id + " to "
				+ cumulativeProduction);
		this.cumulativeProduction = cumulativeProduction;
		updateSurplus();
	}

	public double getCumulativeDemand() {
		return cumulativeDemand;
	}

	public void setCumulativeDemand(double cumulativeDemand) {
		logger.debug("Setting the cum. demand of item " + id + " to "
				+ cumulativeDemand);
		this.cumulativeDemand = cumulativeDemand;
		updateSurplus();
	}

	private void updateSurplus() {
		surplus = cumulativeProduction - cumulativeDemand;
		inventory = Math.max(surplus, 0);
		backlog = Math.max(-surplus, 0);
		logger.trace("Item " + id + " surplus: " + surplus + " inventory: "
				+ inventory + " backlog: " + backlog);
	}
	
	public boolean onTarget() {
		return Math.abs(surplus - surplusTarget) < Sim.SURPLUS_TOLERANCE ? true
				: false;
	}

	public boolean onOrAboveTarget() {
		return surplus >= surplusTarget - Sim.SURPLUS_TOLERANCE;
	}
	
	/**
	 * Computes the time required to reach a given surplus level assuming a fluid model
	 * with no failures. The time computed <em>excludes setup time</em>. 
	 * 
	 * @param surplusLevel
	 * @return double
	 */
	public double getFluidTimeToSurplusLevel(double surplusLevel) {
		double surplusDiff = surplusLevel - this.getSurplus();
		if (surplusDiff <= 0){
			return -surplusDiff/this.getDemandRate();
		} else{
			return surplusDiff/(productionRate - demandRate);
		}
	}
	
	public int getId() {
		return id;
	}

	public double getSurplus() {
		return surplus;
	}

	public double getSurplusDeviation() {
		return surplusTarget - surplus;
	}
	
	public double getSurplusTarget() {
		return surplusTarget;
	}

	public double getInventory() {
		return inventory;
	}

	public double getBacklog() {
		return backlog;
	}

	public double getDemandRate() {
		return demandRate;
	}

	public double getProductionTime() {
		return productionRate;
	}

	public double getInventoryCostRate() {
		return inventoryCostRate;
	}

	public double getBacklogCostRate() {
		return backlogCostRate;
	}

	public double getSetupTime() {
		return setupTime;
	}

	public double getProductionRate() {
		return productionRate;
	}

	public boolean isUnderProduction(){
		return isUnderProduction;
	}
	
	public void setUnderProduction(){
		if(logger.isTraceEnabled()){
			logger.trace("Setting " + this + " under production");
		}
		isUnderProduction=true;
	}
	
	public void unsetUnderProduction(){
		if (logger.isTraceEnabled()){
			logger.trace("Unsetting " + this + " under production");
		}
		isUnderProduction=false;
	}
	
	
}
