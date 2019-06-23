package lowerbounds;

import java.util.Map;

import com.google.common.collect.Maps;

import lombok.extern.apachecommons.CommonsLog;
import optimization.DoubleIndexOptimizationVar;
import optimization.Monomial;
import optimization.OptimizationProblem;
import optimization.OptimizationVar;
import optimization.Posynomial;
import optimization.SingleIndexOptimizationVar;
import params.Params;

/**
 * Solves a lower bound optimization problem that minimizes the cost given by
 * <pre>
 *      (1/2) d_i * ( 1 - rho_i / e ) * (hi * bi) / (hi + bi) * ( 1 - pic )^2 / ni.
 * </pre>
 * 
 */
@CommonsLog
public class SurplusCostLowerBound extends AbstractLowerBound {

    private Map<Integer, Double> idealSurplusDeviation;

    public SurplusCostLowerBound(String name, Params params) {
        super(name, params);
        idealSurplusDeviation = Maps.newHashMap();
    }

    @Override
    public Posynomial getUnscaledObjectivePosynomial(Params params, SingleIndexOptimizationVar<Integer> setupFreq,
            DoubleIndexOptimizationVar<Integer, Integer> transitionFreq,
            SingleIndexOptimizationVar<Integer> cruisingFrac, double scalingFactor) {
        final Posynomial objPosynomial = new Posynomial();
        for (int i = 0; i < params.getNumItems(); i++) {

            double bI = params.getBacklogCosts().get(i);
            double hI = params.getInventoryHoldingCosts().get(i);

            // Calculate the factor hb/(h+b) as 1/(1/h + 1/b) so that's
            // numerically stable
            double cI = 1 / (1 / hI + 1 / bI);
            double dI = params.getDemandRates().get(i);
            // Get the production time and compensate by the machine efficiency
            double compTaoI = (1.0 / params.getProductionRates().get(i)) / params.getMachineEfficiency();
            double compRhoI = dI * compTaoI;
            OptimizationVar nI = setupFreq.get(i);
            OptimizationVar pcI = cruisingFrac.get(i);
            // First compute the coefficient
            double coeff = cI * compRhoI * ( 1 - compRhoI ) / ( 2 * compTaoI );
            // Now expand (1 - pc)^2/ni and add each term from (1 - 2pc + pc^2)/ni
            Monomial m1 = new Monomial();
            Monomial m2 = new Monomial();
            Monomial m3 = new Monomial();
            // coeff / (nI_scaled * scaling_factor)
            m1.mult(coeff / scalingFactor).mult(nI, -1.0);
            // - 2 coeff pcI_scaled / nI_scaled
            m2.mult(coeff).mult(-2).mult(pcI, 1).mult(nI, -1.0);
            // coeff * (pcI_scaled * scaling_factor)^2 / ( nI_sc * scaling_factor )
            m3.mult(coeff).mult( scalingFactor ).mult(pcI, 2).mult(nI, -1.0);
            objPosynomial.add(m1).add(m2).add(m3);
        }
        return objPosynomial;
    }

    @Override
    public void compute() throws Exception {

        super.compute();

        // Compute the ideal surplus deviations (see 4.16 of Tubilla 2011)
        for (int i = 0; i < params.getNumItems(); i++) {
            double dI = params.getDemandRates().get(i);
            // Get the production time and compensate by the machine efficiency
            double compTaoI = (1.0 / params.getProductionRates().get(i)) / params.getMachineEfficiency();
            double compRhoI = dI * compTaoI;
            double pcI = getCruisingFrac(i);
            double nI = getIdealFrequency(i);
            double yI = compRhoI * (1 - compRhoI) * (1 - pcI) / (nI * compTaoI);
            idealSurplusDeviation.put(i, yI);
            log.debug(String.format("The ideal surplus deviation of item %d is %.5f", i, yI));
        }
    }

    public Double getIdealSurplusDeviation(int itemId) {
        return idealSurplusDeviation.get(itemId);
    }

    @Override
    void beforeSolve(OptimizationProblem optimizationProblem, SingleIndexOptimizationVar<Integer> setupFreq,
            DoubleIndexOptimizationVar<Integer, Integer> transitionFreq,
            SingleIndexOptimizationVar<Integer> nonCruisingFrac, double scalingFactor) {
        // Nothing to do
    }

}
