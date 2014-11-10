package optimization;

import java.util.Map;

import lombok.extern.apachecommons.CommonsLog;

import com.google.common.collect.Maps;

@CommonsLog
public class OptimizationConstraint {

	public static enum Sense {EQ, LEQ, GEQ};
	
	private final Map<OptimizationVar, Double> leftHandExpr = Maps.newLinkedHashMap();
	private StringBuilder expression = new StringBuilder();
	private Sense sense;
	private Double rightHandTerm = null;
	
	public OptimizationConstraint(String name) {
		this.expression.append(name).append(": ");
	}
	
	/**
	 * Adds a term to the constraint (on the left hand side).
	 * 
	 * @param var
	 * @param coeff
	 * @return optimizationConstraint
	 */
	public OptimizationConstraint addTerm(OptimizationVar var, double coeff) {
		assert rightHandTerm == null : "Cannot add a term after setting the right hand side";
		if (leftHandExpr.containsKey(var)){			
			log.debug("Overwriting the term for " + var + " in " + this);
		}	
		String coeffStr = coeff == 1.0 ? "" : String.format("%.5f", coeff);
		if (!leftHandExpr.isEmpty() && coeff > 0) {
			expression.append("+ ").append(coeffStr).append(var).append(" ");
		} else {
			expression.append(coeffStr).append(var).append(" ");
		}
		leftHandExpr.put(var, coeff);	
		return this;
	}
	
	/**
	 * Sets the constraint equal to the given value and prevents any further addition of terms.
	 * 
	 * @param rightHandTerm
	 */
	public void eql(double rightHandTerm) {
		this.rightHandTerm = rightHandTerm;
		sense = Sense.EQ;
		expression.append("= ").append(rightHandTerm);
	}
	
	/**
	 * Sets the constraint to less-equal than the given value and prevents any further addition of terms.
	 * 
	 * @param rightHandTerm
	 */
	public void lEql(double rightHandTerm) throws Exception {
		this.rightHandTerm = rightHandTerm;
		sense = Sense.LEQ;
		expression.append("<= ").append(rightHandTerm);
	}
	
	/**
	 * Sets the constraint to greater-equal than the given value and prevents any further addition of terms.
	 * 
	 * @param rightHandTerm
	 */
	public void gEql(double rightHandTerm) {
		this.rightHandTerm = rightHandTerm;
		sense = Sense.GEQ;
		expression.append(">= ").append(rightHandTerm);
	}
	
	public double getCoeff(OptimizationVar var) {
		if (leftHandExpr.containsKey(var)){
			return leftHandExpr.get(var);
		} else {
			return 0.0;
		}
	}

	public Double getRightHandSide() {
		return this.rightHandTerm;
	}
	
	public Sense getSense() {
		return sense;
	}
	
	@Override
	public String toString() {
		return expression.toString();
	}
	
}


