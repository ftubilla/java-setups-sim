/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * © 2012 Fernando Tubilla. All rights reserved.
 */

package system;

import sim.Params;

public class Item {
	
	// Parameters
	private double demandRate;
	private double productionTime;
	private double deviationCostRate;
	private double inventoryCostRate;
	private double backlogCostRate;
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
	public void setDemandRate(double demandRate) {
		this.demandRate = demandRate;
	}
	public double getProductionTime() {
		return productionTime;
	}
	public void setProductionTime(double productionTime) {
		this.productionTime = productionTime;
	}
	public double getDeviationCostRate() {
		return deviationCostRate;
	}
	public void setDeviationCostRate(double deviationCostRate) {
		this.deviationCostRate = deviationCostRate;
	}
	public double getInventoryCostRate() {
		return inventoryCostRate;
	}
	public void setInventoryCostRate(double inventoryCostRate) {
		this.inventoryCostRate = inventoryCostRate;
	}
	public double getBacklogCostRate() {
		return backlogCostRate;
	}
	public void setBacklogCostRate(double backlogCostRate) {
		this.backlogCostRate = backlogCostRate;
	}
	
	

}
