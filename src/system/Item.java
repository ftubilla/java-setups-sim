/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * © 2012 Fernando Tubilla. All rights reserved.
 */

package system;

import sim.*;

public class Item {
	
	// Parameters
	private double demandRate;
	private double productionRate;
	private double setupTime;
	private double deviationCostRate;
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
	
	
	
	public int getId() {
		return id;
	}


	public Item(int id, Params params){
		this.id = id;
		demandRate = params.getDemandRates().get(id);
		productionRate = params.getProductionRates().get(id);
		setupTime = params.getSetupTimes().get(id);
		setCumulativeDemand(params.getInitialDemand().get(id));
		surplusTarget = params.getSurplusTargets().get(id);
	}
	
	
	public double getCumulativeProduction() {
		return cumulativeProduction;
	}
	
	public void setCumulativeProduction(double cumulativeProduction) {
		this.cumulativeProduction = cumulativeProduction;
		updateSurplus();
	}
	
	public double getCumulativeDemand() {
		return cumulativeDemand;
	}
	
	public void setCumulativeDemand(double cumulativeDemand) {
		this.cumulativeDemand = cumulativeDemand;
		updateSurplus();
	}
	
	private void updateSurplus(){
		surplus = cumulativeProduction - cumulativeDemand;
		inventory = Math.max(surplus,0);
		backlog = Math.max(-surplus,0);
	}
	
	public boolean onTarget(){
		return Math.abs(surplus - surplusTarget) < Sim.SURPLUS_TOLERANCE ? true : false;
	}
	
	public boolean onOrAboveTarget(){
		return surplus >= surplusTarget - Sim.SURPLUS_TOLERANCE;
	}
	
	public double workToTarget(){
		return Math.max(0, (surplusTarget-surplus)/(productionRate - demandRate) );
	}
	
	public double getSurplus() {
		return surplus;
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

	public double getDeviationCostRate() {
		return deviationCostRate;
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

	
	

}
