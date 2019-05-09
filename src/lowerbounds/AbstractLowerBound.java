package lowerbounds;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.joptimizer.functions.ConvexMultivariateRealFunction;

import lombok.Getter;
import lombok.extern.apachecommons.CommonsLog;
import optimization.DoubleIndexOptimizationVar;
import optimization.OptimizationConstraint;
import optimization.OptimizationProblem;
import optimization.OptimizationVar;
import optimization.Posynomial;
import optimization.SingleIndexOptimizationVar;
import params.Params;

/**
 * Computes the lower bound surplus cost of a system (see Tubilla (2011)). The
 * equations are scaled by (1 - rho/e), which improves the numerical properties
 * of the model.
 * 
 * @author ftubilla
 *
 */
@CommonsLog
public abstract class AbstractLowerBound {

    // TODO Add more unit tests to ensure that things are working properly!

    protected final Params params;
    private final String name;
    @VisibleForTesting @Getter protected final SingleIndexOptimizationVar<Integer> setupFreq;
    @VisibleForTesting @Getter protected final DoubleIndexOptimizationVar<Integer, Integer> transitionFreq;
    @VisibleForTesting @Getter protected final SingleIndexOptimizationVar<Integer> cruisingFrac;

    private final OptimizationProblem opt;
    private final List<Integer> items;
    // Scale by (1-rho/e)
    @VisibleForTesting protected final double scalingFactor;

    @Getter private Double lowerBound;
    @Getter private Boolean isCruising;

    public AbstractLowerBound(String name, Params params) {
        this.name = name;
        this.params = params;
        items = new ArrayList<Integer>(params.getNumItems());
        double rhoOverEff = 0;
        for (int i = 0; i < params.getNumItems(); i++) {
            items.add(i);
            double itemRhoOverEff = params.getDemandRates().get(i) / params.getProductionRates().get(i)
                    / params.getMachineEfficiency();
            rhoOverEff += itemRhoOverEff;
        }
        this.scalingFactor = 1 - rhoOverEff;
        log.debug(String.format("Scaling variables by (1 - rho/e) = %.5f", this.scalingFactor));
        // Make the variables (note that these variables are scaled by the
        // scaling factor; e.g., n is actually n / (1 - rho/e)
        setupFreq = new SingleIndexOptimizationVar<Integer>("n", items);
        transitionFreq = new DoubleIndexOptimizationVar<Integer, Integer>("n", items, items);
        cruisingFrac = new SingleIndexOptimizationVar<Integer>("pc", items);
        opt = new OptimizationProblem(name + "_" + params.getFile());
        opt.addVar(setupFreq);
        opt.addVar(transitionFreq);
        opt.addVar(cruisingFrac);
    }

    /**
     * Generates the objective function to minimize using the <i>original, unscaled</i> variables. That is,
     * all variables are multiplied by the given scaling factor in order to recover the original (unscaled)
     * optimization objective.
     * 
     * @param params
     * @param setupFreq
     * @param transitionFreq
     * @param nonCruisingFrac
     * @param scalingFactor
     * @return objective function
     */
    public abstract Posynomial getUnscaledObjectivePosynomial(Params params, SingleIndexOptimizationVar<Integer> setupFreq,
            DoubleIndexOptimizationVar<Integer, Integer> transitionFreq,
            SingleIndexOptimizationVar<Integer> nonCruisingFrac,
            double scalingFactor);

    public void compute() throws Exception {

        final Posynomial objPosynomial = getUnscaledObjectivePosynomial(params, setupFreq, transitionFreq, cruisingFrac, scalingFactor);
        // Re-scale the objective, since as 1 - rho/e tends to 0 the obj tends to grow
        objPosynomial.mult(this.scalingFactor);
        ConvexMultivariateRealFunction objFunction = objPosynomial.getConvexFunction(opt.getVariablesMap());
        opt.setObj(objFunction);

        // Create the constraints
        // Time fractions
        // sum_i (n_i*S_i + pc_i*(1-rho_i)) = 1 (after dividing by the scaling
        // 1-rho/e)
        OptimizationConstraint timeFractionsCtr = new OptimizationConstraint("time_fractions");
        double eff = params.getMachineEfficiency();
        for (Integer i : items) {
            double dI = params.getDemandRates().get(i);
            // Get the production rate and compensate by the machine efficiency
            double compTaoI = (1.0 / params.getProductionRates().get(i)) / eff;
            timeFractionsCtr.addTerm(setupFreq.get(i), params.getSetupTimes().get(i));
            timeFractionsCtr.addTerm(cruisingFrac.get(i), 1 - dI * compTaoI);
        }
        timeFractionsCtr.eql(1);
        opt.addConstraint(timeFractionsCtr);

        // TransitionFrequencies
        // Setup into
        // sum_i n_ij = nj
        for (Integer j : items) {
            OptimizationConstraint ctr = new OptimizationConstraint("setup_freq_to_" + j);
            for (Integer i : items) {
                ctr.addTerm(transitionFreq.get(i, j), 1.0);
            }
            ctr.addTerm(setupFreq.get(j), -1.0);
            ctr.eql(0.0);
            opt.addConstraint(ctr);
        }

        // Setup out of
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

        // No self-setups
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

        // Nonnegativity
        // It is important to exclude the self-setup variables because they're
        // already =0 and
        // JOptimizer's interior point method might fail if we add redundant
        // constraints
        for (OptimizationVar var : opt.getVariables()) {
            if (!selfSetupVars.contains(var)) {
                OptimizationConstraint ctr = new OptimizationConstraint("nonnegative_" + var);
                ctr.addTerm(var, 1.0);
                ctr.gEql(0.0);
                opt.addConstraint(ctr);
            }
        }

        // Upper bounds
        for (int i : items) {
            OptimizationConstraint ctr = new OptimizationConstraint("UB_cruising_frac_" + i);
            ctr.addTerm(cruisingFrac.get(i), 1.0);
            ctr.lEql(1.0);
            opt.addConstraint(ctr);
        }

        // Solve and store the solution
        log.debug(String.format("Lower bound %s\nObjective: %s\nConstraints:\n%s", this, objPosynomial, opt));
        opt.setTolerance(1e-5);
        opt.setToleranceFeas(1e-5);
        opt.solve();
        // Unscale the cost
        this.lowerBound = opt.getOptimalCost() / this.scalingFactor;
        log.debug(String.format("The lower bound cost is %.5f", this.lowerBound));

        // Assume that the bound does not prescribe cruising and then check
        this.isCruising = false;
        for (int i : items) {
            log.debug(String.format("The ideal setup frequency of item %d is %.5f", i, getIdealFrequency(i)));
            log.debug(String.format("The ideal cruising fraction of item %d is %.5f", i, getCruisingFrac(i)));
            if (getCruisingFrac(i) > 0.0 + 1e-5) {
                // TODO Set this tolerance in a better way, perhaps as a
                // function of the cruising fraction
                log.debug(String.format("The policy should cruise because of item %d", i));
                this.isCruising = true;
            }
        }
    }

    public Double getIdealFrequency(int itemId) {
        return setupFreq.get(itemId).getSol() * this.scalingFactor;
    }

    public Double getCruisingFrac(int itemId) {
        return cruisingFrac.get(itemId).getSol() * this.scalingFactor;
    }

    public Double getTransitionFreq(int fromItem, int toItem) {
        return transitionFreq.get(fromItem, toItem).getSol() * this.scalingFactor;
    }

    public int getNumItems() {
        return items.size();
    }

    @Override
    public String toString() {
        return name;
    }

}
