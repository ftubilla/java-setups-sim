package lowerbounds;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lombok.Getter;
import lombok.extern.apachecommons.CommonsLog;
import optimization.DoubleIndexOptimizationVar;
import optimization.OptimizationConstraint;
import optimization.OptimizationProblem;
import optimization.OptimizationVar;
import optimization.Posynomial;
import optimization.SingleIndexOptimizationVar;
import params.Params;

import com.google.common.collect.Sets;
import com.joptimizer.functions.ConvexMultivariateRealFunction;

/**
 * Computes the lower bound surplus cost of a system (see Tubilla (2011)).
 * 
 * @author ftubilla
 *
 */
@CommonsLog
public abstract class AbstractLowerBound {

	protected final Params params;
	private final String name;
	@Getter private Double lowerBound;
	@Getter private Boolean isCruising;
	private final SingleIndexOptimizationVar<Integer> setupFreq;
	private final DoubleIndexOptimizationVar<Integer, Integer> transitionFreq;
	private final SingleIndexOptimizationVar<Integer> nonCruisingFrac;
	private final OptimizationProblem opt;
	private final List<Integer> items;
	
	public AbstractLowerBound(String name, Params params) {
		this.name = name;
		this.params = params;
		items = new ArrayList<Integer>(params.getNumItems());
		for (int i=0; i<params.getNumItems(); i++){
			items.add(i);
		}
		//Make the variables
		setupFreq = new SingleIndexOptimizationVar<Integer>("n", items);
		transitionFreq = new DoubleIndexOptimizationVar<Integer, Integer>("n", items, items);
		nonCruisingFrac = new SingleIndexOptimizationVar<Integer>("pnc", items);
		opt = new OptimizationProblem(name + "_" + params.getFile()); 
		opt.addVar(setupFreq);
		opt.addVar(transitionFreq);
		opt.addVar(nonCruisingFrac);
	}
	
	public abstract Posynomial getObjectivePosynomial(Params params, 
			SingleIndexOptimizationVar<Integer> setupFreq,
			DoubleIndexOptimizationVar<Integer, Integer> transitionFreq,
			SingleIndexOptimizationVar<Integer> nonCruisingFrac);
	
	public void compute() throws Exception {
		
		final Posynomial objPosynomial = getObjectivePosynomial(params, setupFreq, transitionFreq, nonCruisingFrac);
		ConvexMultivariateRealFunction objFunction = objPosynomial.getConvexFunction(opt.getVariablesMap());
		opt.setObj(objFunction);
		
		//Create the constraints		
		//Time fractions
		// sum_i (n_i*S_i - pnc_i*(1-rho_i)) = 1 - N
		OptimizationConstraint timeFractionsCtr = new OptimizationConstraint("time_fractions");
		for (Integer i : items) {
			double dI = params.getDemandRates().get(i);
			double taoI = 1.0 / params.getProductionRates().get(i);
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
		Set<OptimizationVar> selfSetupVars = Sets.newHashSet();
		for (Integer i : items) {
			OptimizationConstraint ctr = new OptimizationConstraint("no_self_setup_" + i);
			OptimizationVar selfSetup = transitionFreq.get(i, i);
			ctr.addTerm(selfSetup, 1.0);
			ctr.eql(0.0);
			opt.addConstraint(ctr);
			selfSetupVars.add(selfSetup);
		}
		
		//Nonnegativity
		//It is important to exclude the self-setup variables because they're already =0 and
		//JOptimizer's interior point method might fail if we add redundant constraints
		for (OptimizationVar var : opt.getVariables()) {
			if (!selfSetupVars.contains(var)) {
				OptimizationConstraint ctr = new OptimizationConstraint("nonnegative_" + var);
				ctr.addTerm(var, 1.0);
				ctr.gEql(0.0);
				opt.addConstraint(ctr);
			}
		}
		
		//Upper bounds
		for (int i : items){
			OptimizationConstraint ctr = new OptimizationConstraint("UB_noncruising_frac_"+i);
			ctr.addTerm(nonCruisingFrac.get(i), 1.0);
			ctr.lEql(1.0);
			opt.addConstraint(ctr);
		}
		
		//Solve and store the solution
		log.debug(String.format("Lower bound %s\nObjective: %s\nConstraints:\n%s", this, objPosynomial, opt));
		opt.setTolerance(1e-5);
		opt.setToleranceFeas(1e-5);
		opt.solve();
		lowerBound = opt.getOptimalCost();
		log.debug(String.format("The lower bound cost is %.5f", lowerBound));
		for (int i : items){
			log.debug(String.format("The ideal setup frequency of item %d is %.5f", i, setupFreq.get(i).getSol()));
			log.debug(String.format("The ideal cruising fraction of item %d is %.5f", 1, 
					1-nonCruisingFrac.get(i).getSol()));
		}
				
	}	
	
	public Double getIdealFrequency(int itemId) {
		return setupFreq.get(itemId).getSol();
	}
	
	public Double getCruisingFrac(int itemId) {
		return 1 - nonCruisingFrac.get(itemId).getSol();
	}
	
	public Double getTransitionFreq(int fromItem, int toItem) {
		return transitionFreq.get(fromItem, toItem).getSol();
	}
	
	@Override
	public String toString(){
		return name;
	}
	
}


