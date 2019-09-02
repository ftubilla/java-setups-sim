/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * � 2012 Fernando Tubilla. All rights reserved.
 */

package system;

import org.apache.log4j.Logger;

import com.google.common.annotations.VisibleForTesting;

import lombok.Getter;
import params.Params;
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
    private final double demandRate;
    private final double productionRate;
    @Getter private final double utilization;
    private final double setupTime;
    private final double inventoryCostRate;
    private final double backlogCostRate;
    private final double cCostRate;
    private final double surplusTarget;
    private final int    id;

    // Variables
    private double  cumulativeProduction;
    private double  cumulativeDemand;
    private double  surplus;
    private double  inventory;
    private double  backlog;
    private boolean isUnderProduction = false;

    public Item(int id, Params params) {
        this.id = id;
        this.demandRate = params.getDemandRates().get(id);
        this.productionRate = params.getProductionRates().get(id);
        this.utilization = this.demandRate / this.productionRate;
        this.setupTime = params.getSetupTimes().get(id);
        setCumulativeDemand(params.getInitialDemand().get(id));
        this.surplusTarget = params.getSurplusTargets().get(id);
        this.inventoryCostRate = params.getInventoryHoldingCosts().get(id);
        this.backlogCostRate = params.getBacklogCosts().get(id);
        this.cCostRate = computeCCost(params.getBacklogCosts().get(id), params.getInventoryHoldingCosts().get(id));
        logger.debug(String.format(
                "Created item %d with demand rate %.3f, production rate %.3f, setup time %.3f, surplus target %.3f, "
                        + "inventory holding cost %.3f, backlog cost %.3f, c-cost %.3f",
                this.id, this.demandRate, this.productionRate, this.setupTime, this.surplusTarget,
                this.inventoryCostRate, this.backlogCostRate, this.cCostRate));
    }

    public String toString() {
        return "Item:" + id;
    }

    public double getCumulativeProduction() {
        return cumulativeProduction;
    }

    public void setCumulativeProduction(double cumulativeProduction) {
        logger.debug("Setting the cum. production of item " + id + " to " + cumulativeProduction);
        this.cumulativeProduction = cumulativeProduction;
        updateSurplus();
    }

    public double getCumulativeDemand() {
        return cumulativeDemand;
    }

    public void setCumulativeDemand(double cumulativeDemand) {
        logger.debug("Setting the cum. demand of item " + id + " to " + cumulativeDemand);
        this.cumulativeDemand = cumulativeDemand;
        updateSurplus();
    }

    private void updateSurplus() {
        surplus = cumulativeProduction - cumulativeDemand;
        inventory = Math.max(surplus, 0);
        backlog = Math.max(-surplus, 0);
        logger.trace("Item " + id + " surplus: " + surplus + " inventory: " + inventory + " backlog: " + backlog);
    }

    public boolean onTarget() {
        return Math.abs(surplus - surplusTarget) < Sim.SURPLUS_TOLERANCE ? true : false;
    }

    public boolean onOrAboveTarget() {
        return onOrAboveSurplusValue(surplusTarget);
    }

    public boolean onOrAboveSurplusValue(double surplusValue) {
        return surplus >= surplusValue - Sim.SURPLUS_TOLERANCE;
    }

    /**
     * Computes the time required to reach a given surplus level assuming a
     * fluid model with no failures. The time computed
     * <em>excludes setup time</em>.
     * 
     * @param surplusLevel
     * @return double
     */
    public double getFluidTimeToSurplusLevel(double surplusLevel) {
        double surplusDiff = surplusLevel - this.getSurplus();
        if (surplusDiff <= 0) {
            return -surplusDiff / this.getDemandRate();
        } else {
            return surplusDiff / (productionRate - demandRate);
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

    /**
     * The c-cost rate is defined as the ratio of
     * <pre>
     *      backlog_cost * inventory_cost / ( backlog_cost + inventory_cost )
     * </pre>
     * Note that when either of the backlog or inventory costs are infinite, this expression reduces to the
     * other cost.
     * 
     * @return c-cost
     */
    public double getCCostRate() {
        return this.cCostRate;
    }

    public double getSetupTime() {
        return setupTime;
    }

    public double getProductionRate() {
        return productionRate;
    }

    public boolean isUnderProduction() {
        return isUnderProduction;
    }

    public void setUnderProduction() {
        if (logger.isTraceEnabled()) {
            logger.trace("Setting " + this + " under production");
        }
        isUnderProduction = true;
    }

    public void unsetUnderProduction() {
        if (logger.isTraceEnabled()) {
            logger.trace("Unsetting " + this + " under production");
        }
        isUnderProduction = false;
    }

    @VisibleForTesting
    protected static double computeCCost(final double b, final double h) {
        if ( Double.isFinite(b) && Double.isFinite(h) ) {
            return b * h / ( b + h );
        } else {
            if ( Double.isFinite(b) ) {
                return b;
            } else {
                if ( Double.isFinite(h) ) {
                    return h;
                } else {
                    return Double.POSITIVE_INFINITY;
                }
            }
        }
    }

}
