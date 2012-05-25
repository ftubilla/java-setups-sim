package system;

public class Item {
	
	private double demandRate;
	private double productionTime;
	private double deviationCostRate;
	private double inventoryCostRate;
	private double backlogCostRate;
	
	
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
