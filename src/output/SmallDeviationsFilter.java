package output;

import sim.Sim;
import system.Item;

public class SmallDeviationsFilter implements IFilter {
	
	private double cutoff;
	private Item item;
	
	public SmallDeviationsFilter(Item item, double cutoff){
		this.cutoff = cutoff;
	}
	
	
	@Override
	public boolean passFilter(Sim sim) {
		
		
		if ( item.getSurplusDeviation() >= cutoff){
			return true;
		} else{
			return false;
		}

	}


}
