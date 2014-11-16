package optimization;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;
import optimization.OptimizationConstraint.Sense;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.joptimizer.functions.ConvexMultivariateRealFunction;
import com.joptimizer.functions.LinearMultivariateRealFunction;
import com.joptimizer.optimizers.JOptimizer;
import com.joptimizer.optimizers.LPOptimizationRequest;
import com.joptimizer.optimizers.LPPrimalDualMethod;
import com.joptimizer.optimizers.OptimizationRequest;

/**
 * Encapsulates a JOptimizer problem and constructs the model. This class
 * is not meant to be efficient for large problems!
 * 
 * @author ftubilla
 *
 */
@CommonsLog
public class OptimizationProblem {

	public static enum Response {SUCCESS, WARN, FAILED};
	public static final double DEFAULT_TOL = 1e-6;
	public static final double DEFAULT_TOL_FEAS = 1e-6;
	
	@Getter @Setter private double toleranceFeas = DEFAULT_TOL_FEAS;
	@Getter @Setter private double tolerance = DEFAULT_TOL;
	@Getter private int numVariables;
	@Getter private int numInequalities;
	@Getter private int numEqualities;
	@Setter private boolean userDefinedInitialValues;
	private final StringBuilder stringBuilder = new StringBuilder();
	private final Map<OptimizationVar, Integer> variablesMap = Maps.newLinkedHashMap();
	private final Map<OptimizationConstraint, Integer> inequalities = Maps.newLinkedHashMap();
	private final Map<OptimizationConstraint, Integer> equalities = Maps.newLinkedHashMap();
	private ConvexMultivariateRealFunction objectiveFunction;
	private final OptimizationRequest optimizationRequest;
	private final LPOptimizationRequest initialPointOptimizationRequest;
	private Double optimalCost;
	
	public OptimizationProblem(String name){
		stringBuilder.append(name).append("\n");
		this.optimizationRequest = new OptimizationRequest();
		this.initialPointOptimizationRequest = new LPOptimizationRequest();
		this.userDefinedInitialValues = false;
	}
	
	public void addVar(OptimizationVar var) {		
		if (variablesMap.containsKey(var)){
			log.warn(var + " will not be added because it already exists");
		} else {		
			variablesMap.put(var, numVariables++);
		}
	}
	
	public <I> void addVar(SingleIndexOptimizationVar<I> vars) {
		for (I index : vars) {
			addVar(vars.get(index));
		}
	}
	
	public <I, J> void addVar(DoubleIndexOptimizationVar<I, J> vars) {
		for (I indexI : vars){
			for (J indexJ : vars.get(indexI)) {
				addVar(vars.get(indexI, indexJ));
			}
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
		return variablesMap.get(var);
	}
	
	public Iterable<OptimizationVar> getVariables() {
		return variablesMap.keySet();
	}
	
	public ImmutableMap<OptimizationVar, Integer> getVariablesMap() {
		return ImmutableMap.copyOf(variablesMap);
	}
	
	public Response solve() throws Exception {
		
		if (numInequalities > 0) {
			// Build the inequalities G x <= h
			double[][] matrixG = new double[numInequalities][numVariables];
			double[] vectorH = new double[numInequalities];
			ConvexMultivariateRealFunction[] inequalitiesFunc = new ConvexMultivariateRealFunction[numInequalities];
			for (OptimizationConstraint ctr : inequalities.keySet()) {
				int i = inequalities.get(ctr);
				// If the inequality is >= then change the sign of all
				// coefficients
				int sign = ctr.getSense() == Sense.LEQ ? 1 : -1;
				for (OptimizationVar var : variablesMap.keySet()) {
					int j = variablesMap.get(var);
					matrixG[i][j] = sign * ctr.getCoeff(var);
				}
				vectorH[i] = sign * ctr.getRightHandSide();
				inequalitiesFunc[i] = new LinearMultivariateRealFunction(matrixG[i], -vectorH[i]);
			}
			optimizationRequest.setFi(inequalitiesFunc);
			initialPointOptimizationRequest.setG(matrixG);
			initialPointOptimizationRequest.setH(vectorH);
		}

		if (numEqualities > 0) {
			// Build the equalities A x = b
			double[][] matrixA = new double[numEqualities][numVariables];
			double[] vectorB = new double[numEqualities];
			for (OptimizationConstraint ctr : equalities.keySet()) {
				int i = equalities.get(ctr);
				for (OptimizationVar var : variablesMap.keySet()) {
					int j = variablesMap.get(var);
					matrixA[i][j] = ctr.getCoeff(var);
					log.trace(String.format("A[%d][%d]=%.5f",i,j,matrixA[i][j]));
				}
				vectorB[i] = ctr.getRightHandSide();
			}
			optimizationRequest.setA(matrixA);
			initialPointOptimizationRequest.setA(matrixA);
			optimizationRequest.setB(vectorB);
			initialPointOptimizationRequest.setB(vectorB);
		}
		
		//Set the objective function
		optimizationRequest.setF0(objectiveFunction);
		
		//Set the initial point (equalities-feasible)
		double[] x0;
		if (!userDefinedInitialValues) {
			initialPointOptimizationRequest.setC(new double[numVariables]);
			LPPrimalDualMethod lp = new LPPrimalDualMethod();
			lp.setLPOptimizationRequest(initialPointOptimizationRequest);
			lp.optimize();		
			x0 = lp.getOptimizationResponse().getSolution();
		} else {
			x0 = new double[numVariables];
		}
		for (OptimizationVar var : variablesMap.keySet()){
			int i = variablesMap.get(var);
			if (!userDefinedInitialValues) {
				var.setInitialValue(x0[i]);
			} else {
				x0[i] = var.getInitialValue();
			}
			log.debug(String.format("Initial value %s = %.6f", var, x0[i]));
		}
		optimizationRequest.setNotFeasibleInitialPoint(x0);
		
		optimizationRequest.setCheckKKTSolutionAccuracy(true);
		optimizationRequest.setToleranceFeas(toleranceFeas);
		optimizationRequest.setTolerance(tolerance);
		
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
		for (OptimizationVar var : variablesMap.keySet()){
			var.setSol(sol[variablesMap.get(var)]);
		}
		optimalCost = objectiveFunction.value(sol);
		log.debug(String.format("Finished solving %s with return code %s and objective function value %.5f",
				this, response, optimalCost));

		return response;
	}
	
	@Override
	public String toString() {
		return stringBuilder.toString();
	}
	
}


