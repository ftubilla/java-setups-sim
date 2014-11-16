package lowerbounds;

import optimization.DoubleIndexOptimizationVar;
import optimization.Monomial;
import optimization.OptimizationVar;
import optimization.Posynomial;
import optimization.SingleIndexOptimizationVar;
import params.Params;

public class MakeToOrderLowerBound extends AbstractLowerBound {

	public MakeToOrderLowerBound(String name, Params params) {
		super(name, params);
	}
	
	@Override
	public Posynomial getObjectivePosynomial(Params params, SingleIndexOptimizationVar<Integer> setupFreq,
			DoubleIndexOptimizationVar<Integer, Integer> transitionFreq,
			SingleIndexOptimizationVar<Integer> nonCruisingFrac) {		
		final Posynomial objPosynomial = new Posynomial();
		for (int i=0; i<params.getNumItems(); i++) {
			double cI = params.getBacklogCosts().get(i);
			double dI = params.getDemandRates().get(i);
			double taoI = params.getProductionRates().get(i);
			double rhoI = dI*taoI;
			OptimizationVar nI = setupFreq.get(i);
			OptimizationVar pInc = nonCruisingFrac.get(i);
			Monomial m = new Monomial();
			m.mult(cI).mult(rhoI).mult(1-rhoI).mult(1/taoI).mult(1/2.0).mult(pInc, 2.0).mult(nI, -1.0);
			objPosynomial.add(m);
		}
		return objPosynomial;
	}

}


