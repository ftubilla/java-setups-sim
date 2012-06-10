/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * © 2012 Fernando Tubilla. All rights reserved.
 */

package sim;

import java.util.*;

public class Params {

	private int numItems;
	private List<Double> demandRates;
	private List<Double> productionRates;
	private List<Double> setupTimes;
	private double meanTimeToFail = 1.0;
	private double meanTimeToRepair = 0.0;
	private int initialSetup=0;
	
	
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

	
}
