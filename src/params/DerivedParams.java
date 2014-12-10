package params;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.ToString;
import lowerbounds.MakeToOrderLowerBound;

@ToString
@Getter
public class DerivedParams extends AbstractParams {

	protected double makeToOrderLowerBound;
	protected List<Double> makeToOrderLowerBoundIdealSurplusDeviations;
	protected List<Double> makeToOrderLowerBoundIdealSetupFreq;
	
	public void setMakeToOrderLowerBound(MakeToOrderLowerBound lb){
		makeToOrderLowerBound = lb.getLowerBound();		
		makeToOrderLowerBoundIdealSurplusDeviations = new ArrayList<Double>();
		makeToOrderLowerBoundIdealSetupFreq = new ArrayList<Double>();
		for (int i=0; i<lb.getNumItems(); i++){
			makeToOrderLowerBoundIdealSurplusDeviations.add(lb.getIdealSurplusDeviation(i));
			makeToOrderLowerBoundIdealSetupFreq.add(lb.getIdealFrequency(i));
		}
		
	}
	
}


