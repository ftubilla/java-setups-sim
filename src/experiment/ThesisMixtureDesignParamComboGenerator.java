package experiment;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * THis replicates the mixture design that I used in my thesis.
 * @author ftubilla
 *
 */
public class ThesisMixtureDesignParamComboGenerator implements IParameterComboGenerator {
	private static Logger logger = Logger.getLogger(ThesisMixtureDesignParamComboGenerator.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();

	@SuppressWarnings("unused")
	private boolean trace = logger.isTraceEnabled();

	private double maxRatio;
	private int numPoints = 7;
	private double normalizingValue;
	private List<ParameterCombo> parameterCombos;

	public ThesisMixtureDesignParamComboGenerator(double normalizingValue, double maxRatio){
		this.maxRatio = maxRatio;
		this.normalizingValue = normalizingValue;
	}
		
	
	@Override
	public void generate(int numItems) {
		
		assert numItems == 3 : "Only works with 3 items right now!";

		double denominator = 1.0*(maxRatio + numItems - 1);
		double lowVal = 1/denominator;
		double highVal = maxRatio*lowVal;
		double medVal = (denominator - 1)/2.0/denominator;
		double ctrVal = 1/(1.0*numItems);
		
		Double[][] points = new Double[numPoints][3];
		
		points[0][0] = highVal;
		points[0][1] = lowVal;
		points[0][2] = lowVal;
		
		points[1][0] = medVal;
		points[1][1] = medVal;
		points[1][2] = lowVal;
		
		points[2][0] = lowVal;
		points[2][1] = highVal;
		points[2][2] = lowVal;
		
		points[3][0] = lowVal;
		points[3][1] = medVal;
		points[3][2] = medVal;
		
		points[4][0] = lowVal;
		points[4][1] = lowVal;
		points[4][2] = highVal;
		
		points[5][0] = medVal;
		points[5][1] = lowVal;
		points[5][2] = lowVal;
		
		points[6][0] = ctrVal;
		points[6][1] = ctrVal;
		points[6][2] = ctrVal;
		
		parameterCombos = new ArrayList<ParameterCombo>();
		for (int point=0; point<numPoints; point++){
			ParameterCombo combo = new ParameterCombo(numItems);
			for (int item=0; item<numItems; item++){
				combo.set(item, points[point][item]*normalizingValue);
			}
			parameterCombos.add(combo);
		}
		
	}

	@Override
	public Iterable<ParameterCombo> getParameterCombos() {
		return parameterCombos;
	}
	
	public static void main(String[] args){
		System.out.println("Testing Thesis Mixture Generator");
		ThesisMixtureDesignParamComboGenerator comboGen = new ThesisMixtureDesignParamComboGenerator(1.0, 7);
		comboGen.generate(3);
		for (ParameterCombo combo : comboGen.getParameterCombos()){
			String line = "";
			for (int i=0; i<3; i++){
				line += combo.get(i) + " ";
			}
			System.out.println(line);
		}
		
		
	}
	
}


