package optimization;

import java.util.Map;

import com.google.common.collect.Maps;

public class Monomial {

	private final Map<OptimizationVar, Double> variablesToExponent = Maps.newLinkedHashMap();
	private double coefficient;
	
	public Monomial(double coefficient) {
		this.coefficient = coefficient;
	}
	
	public Monomial() {
		this(1.0);
	}
	
	
	public Monomial mult(double value) {
		this.coefficient = this.coefficient * value;
		return this;
	}
	
	public Monomial mult(OptimizationVar variable, double exponent) {
		if (!variablesToExponent.containsKey(variable)) {
			variablesToExponent.put(variable, 0.0);
		}
		double exp = variablesToExponent.get(variable);
		variablesToExponent.put(variable, exp + exponent);		
		return this;
	}
	
	public Monomial partialDiff(OptimizationVar ofVar) {
		if (!variablesToExponent.containsKey(ofVar)){
			//The term is constant wrt to the given variable
			return new Monomial(0.0);
		}
		Monomial diff = new Monomial(this.coefficient);
		//Add all variables as before but lower the exp of ofVar and multiply the coeff by it
		for (OptimizationVar var : variablesToExponent.keySet()){
			//Put the variable in the diff term
			diff.mult(var, variablesToExponent.get(var));
			if (ofVar.equals(var)){
				diff.mult(variablesToExponent.get(ofVar)); //Multiply the coefficient by the var's exponent
				diff.mult(ofVar, -1.0);					   //Lower the exponent
			}		
		}
		return diff;
	}
	
	public double getCoefficient() {
		return this.coefficient;
	}
	
	public double getExponent(OptimizationVar variable) {
		if (variablesToExponent.containsKey(variable)){
			return variablesToExponent.get(variable);
		} else {
			return 0;
		}
	}
	
	public double eval(Map<OptimizationVar, Double> values) {
		double result = coefficient;
		for (OptimizationVar var : variablesToExponent.keySet()) {
			if (values.containsKey(var)) {
				result = result * Math.pow(values.get(var), variablesToExponent.get(var));
			} else {
				throw new Error("Cannot evaluate " + this + " because no value was given for " + var);
			}
		}
		return result;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(coefficient).append(" ");
		for (OptimizationVar var : variablesToExponent.keySet()){
			if (variablesToExponent.get(var) != 0.0){
				sb.append(var).append("^").append(variablesToExponent.get(var)).append(" ");
			}
		}
		return sb.toString();
	}
	
	
}


