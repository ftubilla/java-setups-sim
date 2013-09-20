package experiment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Represents a combination of values for a parameter across different items.
 * 
 * @author ftubilla
 *
 */
public class ParameterCombo extends Combo<Double>{
	
	private static Logger logger = Logger.getLogger(ParameterCombo.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();

	@SuppressWarnings("unused")
	private boolean trace = logger.isTraceEnabled();
	
	private double[] values;
	private int numItems;
	private boolean hasNegativeValues=false;
	
	public ParameterCombo(int numItems){
		super(numItems);
		values = new double[numItems];
		this.numItems = numItems;
	}
	
	@Override
	public void set(int item, Double value){
		assert item < numItems : "Cannot give a parameter value for item " + item + " if there are only " + numItems;
		values[item] = value;
		if (value < 0){
			hasNegativeValues = true;
		}
	}
	
	@Override
	public Double get(int item){
		return values[item];
	}
	
	@Override
	public void add(Double element){
		throw new UnsupportedOperationException("Use the setter instead!");
	}
	
	public boolean hasNegativeValues(){
		return hasNegativeValues;
	}
	
	/**
	 * Returns true if the parameter combination is the same as the given combination but just with the 
	 * item labels permutated.
	 * 
	 * @param combo
	 * @return boolean
	 */
	public boolean isAPermutationOf(ParameterCombo combo){
		List<Double> list1 = new ArrayList<Double>(numItems);
		for (Double value : values){
			list1.add(value);
		}
		Collections.sort(list1);
		
		List<Double> list2 = new ArrayList<Double>(numItems);
		for (int i=0; i<numItems; i++){
			list2.add(combo.get(i));
		}
		Collections.sort(list2);
		
		for (int i=0; i<numItems; i++){
			if (!list1.get(i).equals(list2.get(i))){
				return false;
			}
		}		
		return true;
	}
	
	@Override
	public String toString(){
		return "ParamCombo:"+values;
	}
	
}


