package optimization;

import java.util.Map;

import lombok.extern.apachecommons.CommonsLog;
import optimization.OptimizationConstraint.Sense;

import com.google.common.collect.Maps;
import com.joptimizer.functions.ConvexMultivariateRealFunction;
import com.joptimizer.functions.LinearMultivariateRealFunction;
import com.joptimizer.optimizers.JOptimizer;
import com.joptimizer.optimizers.OptimizationRequest;

/**
 * Encapsulates a JOptimizer problem
 * @author ftubilla
 *
 */
@CommonsLog
public class OptimizationProblem {

	public static enum Response {SUCCESS, WARN, FAILED};
	
	private int numVariables;
	private int numInequalities;
	private int numEqualities;
	private final StringBuilder stringBuilder = new StringBuilder();
	private final Map<OptimizationVar, Integer> variables = Maps.newLinkedHashMap();
	private final Map<OptimizationConstraint, Integer> inequalities = Maps.newLinkedHashMap();
	private final Map<OptimizationConstraint, Integer> equalities = Maps.newLinkedHashMap();
	private ConvexMultivariateRealFunction objectiveFunction;
	private final OptimizationRequest optimizationRequest;
	
	public OptimizationProblem(String name){
		stringBuilder.append(name).append("\n");
		this.optimizationRequest = new OptimizationRequest();
	}
	
	public void addVar(OptimizationVar var) {		
		if (variables.containsKey(var)){
			log.warn(var + " will not be added because it already exists");
		} else {		
			variables.put(var, numVariables++);
		}
	}
	
	public void addConstraint(OptimizationConstraint ctr){		
		if (inequalities.containsKey(ctr) || equalities.containsKey(ctr)){
			log.warn(ctr + " will not be added because it already exists");
		} else {
			if (ctr.getSense() == Sense.EQ){
				equalities.put(ctr, numEqualities++);
			} else {
				inequalities.put(ctr, numInequalities++);
			}
		}
		stringBuilder.append(ctr.toString()).append("\n");
	}
	
	public void setObj(ConvexMultivariateRealFunction objectiveFunction){
		this.objectiveFunction = objectiveFunction; 
	}
	
	public Integer getIndex(OptimizationVar var){
		return variables.get(var);
	}
	
	public Response solve() throws Exception {
		
		if (numInequalities > 0) {
			// Build the inequalities G x <= h
			ConvexMultivariateRealFunction[] inequalitiesFunc = new ConvexMultivariateRealFunction[numInequalities];
			for (OptimizationConstraint ctr : inequalities.keySet()) {
				int i = inequalities.get(ctr);
				// If the inequality is >= then change the sign of all
				// coefficients
				int sign = ctr.getSense() == Sense.LEQ ? 1 : -1;
				double[] lhs = new double[numVariables];
				for (OptimizationVar var : variables.keySet()) {
					int j = variables.get(var);
					lhs[j] = sign * ctr.getCoeff(var);
				}
				double rhs = sign * ctr.getRightHandSide();
				inequalitiesFunc[i] = new LinearMultivariateRealFunction(lhs, -rhs);
			}
			optimizationRequest.setFi(inequalitiesFunc);
		}

		if (numEqualities > 0) {
			// Build the equalities A x = b
			double[][] matrixA = new double[numEqualities][numVariables];
			double[] vectorB = new double[numEqualities];
			for (OptimizationConstraint ctr : equalities.keySet()) {
				int i = equalities.get(ctr);
				for (OptimizationVar var : variables.keySet()) {
					int j = variables.get(var);
					matrixA[i][j] = ctr.getCoeff(var);
				}
				vectorB[i] = ctr.getRightHandSide();
			}
			optimizationRequest.setA(matrixA);
			optimizationRequest.setB(vectorB);
		}
		
		//Set the objective function
		optimizationRequest.setF0(objectiveFunction);

		//Set the initial point (assume infeasible)
		double[] x0 = new double[numVariables];
		for (OptimizationVar var : variables.keySet()){
			int i = variables.get(var);
			x0[i] = var.getInitialValue();
		}
		optimizationRequest.setNotFeasibleInitialPoint(x0);
		
		optimizationRequest.setCheckKKTSolutionAccuracy(true);
		optimizationRequest.setToleranceFeas(1.E-9);
		optimizationRequest.setTolerance(1.E-9);
		
		//optimization
		JOptimizer opt = new JOptimizer();
		opt.setOptimizationRequest(optimizationRequest);
		int returnCode = opt.optimize();
		Response response = null;
		switch (returnCode) {
		case 0:
			response = Response.SUCCESS;
			break;
		case 1:
			response = Response.WARN;
			break;
		case 2:
			response = Response.FAILED;
			break;
		}
		
		//Save the solution
		double[] sol = opt.getOptimizationResponse().getSolution();
		for (OptimizationVar var : variables.keySet()){
			var.setSol(sol[variables.get(var)]);
		}

		return response;
	}
	
	@Override
	public String toString() {
		return stringBuilder.toString();
	}
	
}


