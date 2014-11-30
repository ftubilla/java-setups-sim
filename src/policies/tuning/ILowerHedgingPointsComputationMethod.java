package policies.tuning;

import sim.Sim;
import system.Item;

public interface ILowerHedgingPointsComputationMethod {

	public void compute(Sim sim);
	
	public double getLowerHedgingPoint(Item item);
	
}


