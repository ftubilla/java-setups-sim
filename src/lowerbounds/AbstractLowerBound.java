package lowerbounds;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.extern.apachecommons.CommonsLog;
import optimization.DoubleIndexOptimizationVar;
import optimization.OptimizationConstraint;
import optimization.OptimizationProblem;
import optimization.OptimizationVar;
import optimization.Posynomial;
import optimization.SingleIndexOptimizationVar;
import params.Params;

import com.joptimizer.functions.ConvexMultivariateRealFunction;

/**
 * Computes the lower bound surplus cost of a system (see Tubilla (2011)).
 * 
 * @author ftubilla
 *
 */
@CommonsLog
public abstract class AbstractLowerBound {

	private final Params params;
	private final String name;
	private Map<Integer, Double> idealFrequencies;
	private Map<Integer, Double> cruisingFraction;
	@Getter private Double lowerBound;
	@Getter private Boolean isCruising;
	
	public AbstractLowerBound(String name, Params params) {
		this.name = name;
		this.params = params;
	}
	
	public abstract Posynomial getObjectivePosynomial(Params params, 
			SingleIndexOptimizationVar<Integer> setupFreq,
			DoubleIndexOptimizationVar<Integer, Integer> transitionFreq,
			SingleIndexOptimizationVar<Integer> nonCruisingFrac);
	
	public void compute() throws Exception {
		
		final List<Integer> items = new ArrayList<Integer>(params.getNumItems());
		for (int i=0; i<params.getNumItems(); i++){
			items.add(i);
		}
		
		//Make the variables
		final SingleIndexOptimizationVar<Integer> setupFreq =
				new SingleIndexOptimizationVar<Integer>("n", items);
		final DoubleIndexOptimizationVar<Integer, Integer> transitionFreq = 
				new DoubleIndexOptimizationVar<Integer, Integer>("n", items, items);
		final SingleIndexOptimizationVar<Integer> nonCruisingFrac =
				new SingleIndexOptimizationVar<Integer>("pnc", items);
		
		final OptimizationProblem opt = new OptimizationProblem("J_opt_" + params.getFile()); 
		opt.addVar(setupFreq);
		opt.addVar(transitionFreq);
		opt.addVar(nonCruisingFrac);
		
		final Posynomial objPosynomial = getObjectivePosynomial(params, setupFreq, transitionFreq, nonCruisingFrac);
		ConvexMultivariateRealFunction objFunction = objPosynomial.getConvexFunction(opt.getVariablesMap());
		opt.setObj(objFunction);
		
		//Create the constraints		
		//Time fractions
		// sum_i (n_i*S_i - pnc_i*(1-rho_i)) = 1 - N
		OptimizationConstraint timeFractionsCtr = new OptimizationConstraint("time_fractions");
		for (Integer i : items) {
			double dI = params.getDemandRates().get(i);
			double taoI = params.getProductionRates().get(i);
			timeFractionsCtr.addTerm(setupFreq.get(i), params.getSetupTimes().get(i));
			timeFractionsCtr.addTerm(nonCruisingFrac.get(i), -(1-dI*taoI));
		}
		timeFractionsCtr.eql(1 - params.getNumItems());
		opt.addConstraint(timeFractionsCtr);
	
		//TransitionFrequencies
		// Setup into
		// sum_i n_ij = nj
		for (Integer j : items) {
			OptimizationConstraint ctr = new OptimizationConstraint("setup_freq_to_" + j);
			for (Integer i : items){
				ctr.addTerm(transitionFreq.get(i, j), 1.0);
			}
			ctr.addTerm(setupFreq.get(j), -1.0);
			ctr.eql(0.0);
			opt.addConstraint(ctr);
		}
		
		//Setup out of
		// sum_i n_ji = nj
		for (Integer j : items) {
			// Don't add the last constraint because it's redundant
			if (j < items.size() - 1) {
				OptimizationConstraint ctr = new OptimizationConstraint("setup_freq_from_" + j);
				for (Integer i : items) {
					ctr.addTerm(transitionFreq.get(j, i), 1.0);
				}
				ctr.addTerm(setupFreq.get(j), -1.0);
				ctr.eql(0.0);
				opt.addConstraint(ctr);
			}
		}
		
		//No self-setups
		// n_ii = 0
		for (Integer i : items) {
			OptimizationConstraint ctr = new OptimizationConstraint("no_self_setup_" + i);
			ctr.addTerm(transitionFreq.get(i, i), 1.0);
			ctr.eql(0.0);
			opt.addConstraint(ctr);
		}
		
		//Nonnegativity
		for (OptimizationVar var : opt.getVariables()) {
			OptimizationConstraint ctr = new OptimizationConstraint("nonnegative_" + var);
			ctr.addTerm(var, 1.0);
			ctr.gEql(0.0);
			opt.addConstraint(ctr);
		}
		
		//Upper bounds
		for (int i : items){
			OptimizationConstraint ctr = new OptimizationConstraint("UB_noncruising_frac_"+i);
			ctr.addTerm(nonCruisingFrac.get(i), 1.0);
			ctr.lEql(1.0);
			opt.addConstraint(ctr);
		}
		
		log.debug(String.format("Lower bound %s\nObjective: %s\nConstraints:\n%s", this, objPosynomial, opt));
		opt.solve();
		for (OptimizationVar var : opt.getVariables()){
			System.out.println(var + "=" + var.getSol());
		}
		
	}	
	
	@Override
	public String toString(){
		return name;
	}
	
}


