/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * © 2012 Fernando Tubilla. All rights reserved.
 */

package system;

import org.apache.log4j.Logger;

import processes.demand.IDemandProcess;
import processes.production.IProductionProcess;
import sim.Clock;
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

	//References
	private Clock clock;
	
	// Variables
	private double cumulativeProduction;
	private double cumulativeDemand;
	private double surplus;
	private double inventory;
	private double backlog;
	private boolean isUnderProduction=false;	

	
	public Item(int id, Clock clock, Params params) {
		this.id = id;
		this.clock = clock;
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

	
	/**
	 * Based on the production and demand processes, returns the earliest possible time interval when
	 * the surplus of the item can reach a predetermined level. If the item is not currently under production,
	 * the setup time is included.
	 * 
	 * @param surplusLevel
	 * @param item
	 * @return time interval
	 */
	public double computeMinDeltaTimeToSurplusLevel(double surplusLevel, IProductionProcess productionProcess,
			IDemandProcess demandProcess) {
							
		if (!productionProcess.isDiscrete() && !demandProcess.isDiscrete()) {
			double surplusDiff = surplusLevel - surplus;
			if (surplusDiff <= 0){
				return -surplusDiff/demandRate;
			} else{
				//If the item is not under production add setup time
				double changeover = isUnderProduction ? 0.0 : setupTime;
				return changeover + surplusDiff/(productionRate-demandRate);
			}
		} else {
			double nextProductionDeparture = productionProcess.getNextScheduledProductionDepartureTime(this);
			if (nextProductionDeparture == Double.MAX_VALUE){
				//If there are no scheduled productions of this item, then the item is not under production
				assert !isUnderProduction;
				nextProductionDeparture = setupTime;
			}
			double nextDemandArrival = demandProcess.getNextScheduledDemandArrivalTime(this);
			return Math.min(nextProductionDeparture, nextDemandArrival) - clock.getTime();
		}		
	}
	
	public double computeMinDeltaTimeToTarget(IProductionProcess productionProcess, IDemandProcess demandProcess){
		return computeMinDeltaTimeToSurplusLevel(surplusTarget, productionProcess, demandProcess);
	}
	
	
	public boolean onTarget() {
		return Math.abs(surplus - surplusTarget) < Sim.SURPLUS_TOLERANCE ? true
				: false;
	}

	public boolean onOrAboveTarget() {
		return surplus >= surplusTarget - Sim.SURPLUS_TOLERANCE;
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
