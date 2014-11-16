package optimization;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.joptimizer.functions.ConvexMultivariateRealFunction;

public class Posynomial implements Iterable<Monomial> {

	private final Set<Monomial> monomials;
	
	public Posynomial() {
		monomials = Sets.newLinkedHashSet();
	}
	
	public Posynomial add(Monomial mon) {
		monomials.add(mon);
		return this;
	}
	
	public Posynomial mult(double coefficient) {
		for (Monomial mon : monomials){
			mon.mult(coefficient);
		}
		return this;
	}
	
	public Posynomial mult(OptimizationVar variable, double exponent) {
		for (Monomial mon : monomials){
			mon.mult(variable, exponent);
		}
		return this;
	}
	
	public Posynomial partialDiff(OptimizationVar ofVar) {
		Posynomial diff = new Posynomial();
		boolean added = false;
		for (Monomial mon : monomials){
			Monomial diffMon = mon.partialDiff(ofVar);
			if (diffMon.getCoefficient() != 0) {
				//Only add terms with non-zero elements
				diff.add(diffMon);
				added = true;
			}			
		}
		if (!added) {
			diff.add(new Monomial(0.0));
		}
		return diff;
	}
	
	public double eval(Map<OptimizationVar, Double> values) {
		double result = 0.0;
		for (Monomial mon : monomials){
			result = result + mon.eval(values);
		}
		return result;
	}
	
	/**
	 * Assumes that the posynomial is a convex function and returns an instance
	 * of ConvexMultivariateRealFunction.
	 * 
	 * @param variablesMap
	 * @return convexMultivariateRealFunction
	 */
	public ConvexMultivariateRealFunction getConvexFunction(final Map<OptimizationVar, Integer> variablesMap) {
		
		final int numVariables = variablesMap.size();
		final Posynomial thisPosynomial = this;
		
		//Compute the gradient
		final Map<OptimizationVar, Posynomial> gradient = new LinkedHashMap<OptimizationVar, Posynomial>();
		for (OptimizationVar varI : variablesMap.keySet()) {
			gradient.put(varI, this.partialDiff(varI));
		}
		
		//Compute the hessian
		final Map<OptimizationVar, Map<OptimizationVar, Posynomial>> hessian = 
				new LinkedHashMap<OptimizationVar, Map<OptimizationVar, Posynomial>>();
		for (OptimizationVar varI : variablesMap.keySet()) {
			hessian.put(varI, new LinkedHashMap<OptimizationVar, Posynomial>() );
			for (OptimizationVar varJ : variablesMap.keySet()) {
				hessian.get(varI).put(varJ, gradient.get(varI).partialDiff(varJ));
			}
		}
	
		ConvexMultivariateRealFunction convexFunction = new ConvexMultivariateRealFunction() {						
			@Override
			public double value(double[] X) {
				//Set the values of the variables
				Map<OptimizationVar, Double> values = new HashMap<OptimizationVar, Double>();
				for (OptimizationVar var : variablesMap.keySet()) {
					values.put(var, X[variablesMap.get(var)]);
				}
				return thisPosynomial.eval(values);
			}						
			@Override
			public double[][] hessian(double[] X) {
				Map<OptimizationVar, Double> values = new HashMap<OptimizationVar, Double>();
				for (OptimizationVar var : variablesMap.keySet()) {
					values.put(var, X[variablesMap.get(var)]);
				}
				double[][] hessMatrix = new double[numVariables][numVariables];
				for (OptimizationVar varI : variablesMap.keySet()) {
					for (OptimizationVar varJ : variablesMap.keySet()) {
						hessMatrix[variablesMap.get(varI)][variablesMap.get(varJ)] = hessian.get(varI).get(varJ).eval(values);
					}
				}
				return hessMatrix;
			}			
			@Override
			public double[] gradient(double[] X) {
				Map<OptimizationVar, Double> values = new HashMap<OptimizationVar, Double>();
				for (OptimizationVar varI : variablesMap.keySet()) {
					values.put(varI, X[variablesMap.get(varI)]);
				}
				double[] gradVector = new double[numVariables];
				for (OptimizationVar varI : variablesMap.keySet()) {
					gradVector[variablesMap.get(varI)] = gradient.get(varI).eval(values);
				}
				return gradVector;
			}				
			@Override
			public int getDim() {
				return numVariables;
			}
		};
		
		return convexFunction;
	}

	@Override
	public Iterator<Monomial> iterator() {
		return monomials.iterator();
	}		
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		boolean isFirst = true;
		for (Monomial mon : monomials){
			if (mon.getCoefficient() > 0 && !isFirst) {
				sb.append("+ ");
			}
			sb.append(mon.toString());
			isFirst = false;
		}
		return sb.toString();
	}
	
}


