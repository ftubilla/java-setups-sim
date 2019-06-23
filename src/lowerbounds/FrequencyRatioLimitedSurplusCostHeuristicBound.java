package lowerbounds;

import lombok.extern.apachecommons.CommonsLog;
import optimization.DoubleIndexOptimizationVar;
import optimization.OptimizationConstraint;
import optimization.OptimizationProblem;
import optimization.SingleIndexOptimizationVar;
import params.Params;

/**
 * Implements and solves the model for obtaining a surplus lower bound, but with
 * an additional set of constraints that limit the max. ratio between setup
 * frequencies of the items to be below <code>N-1</code>. Since the new
 * constraints make this no longer a lower bound, we call it a "heuristic bound"
 * However, this new constraint helps in cases where items have widely different
 * <code>cmu</code> ratios, where the original bound can be quite loose.
 */
@CommonsLog
public class FrequencyRatioLimitedSurplusCostHeuristicBound extends SurplusCostLowerBound {

    public static final double TOLERANCE = 1e-4;

    private final double ratioLimit;
    private final int numItems;

    public FrequencyRatioLimitedSurplusCostHeuristicBound(String name, Params params) {
        super(name, params);
        this.numItems = params.getNumItems();
        this.ratioLimit = this.numItems - 1 + TOLERANCE;
    }

    @Override
    void beforeSolve(OptimizationProblem optimizationProblem, SingleIndexOptimizationVar<Integer> setupFreq,
            DoubleIndexOptimizationVar<Integer, Integer> transitionFreq,
            SingleIndexOptimizationVar<Integer> nonCruisingFrac, double scalingFactor) {
        if ( this.numItems > 2 ) {
            log.debug("Adding a setup frequency ratio constraint to the lower bound calculation");
            // Upper bound on frequencies
            for ( int i = 0; i < this.numItems; i++ ) {
                for ( int j = 0; j < this.numItems; j++ ) {
                    if ( i != j ) {
                        OptimizationConstraint ctr = new OptimizationConstraint(String.format("n_%d <= ratio_limit n_%d", i, j));
                        ctr.addTerm(setupFreq.get(i), 1);
                        ctr.addTerm(setupFreq.get(j), - this.ratioLimit );
                        ctr.lEql(0.0);
                        optimizationProblem.addConstraint(ctr);
                    }
                }
            }
        }
    }

}
