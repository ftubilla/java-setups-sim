package lowerbounds;

import java.util.Map;

import lombok.extern.apachecommons.CommonsLog;
import optimization.DoubleIndexOptimizationVar;
import optimization.Monomial;
import optimization.OptimizationVar;
import optimization.Posynomial;
import optimization.SingleIndexOptimizationVar;
import params.Params;

import com.google.common.collect.Maps;

@CommonsLog
public class MakeToOrderLowerBound extends AbstractLowerBound {

	private Map<Integer, Double> idealSurplusDeviation;
	
	public MakeToOrderLowerBound(String name, Params params) {
		super(name, params);
		idealSurplusDeviation = Maps.newHashMap();
	}
	
	@Override
	public Posynomial getObjectivePosynomial(Params params, SingleIndexOptimizationVar<Integer> setupFreq,
			DoubleIndexOptimizationVar<Integer, Integer> transitionFreq,
			SingleIndexOptimizationVar<Integer> nonCruisingFrac) {		
		final Posynomial objPosynomial = new Posynomial();
		for (int i=0; i < params.getNumItems(); i++) {
			double cI = params.getBacklogCosts().get(i);
			double dI = params.getDemandRates().get(i);
			double taoI = 1.0 / params.getProductionRates().get(i);
			double rhoI = dI*taoI;
			OptimizationVar nI = setupFreq.get(i);
			OptimizationVar pInc = nonCruisingFrac.get(i);
			Monomial m = new Monomial();
			m.mult(cI).mult(rhoI).mult(1-rhoI).mult(1/taoI).mult(1/2.0).mult(pInc, 2.0).mult(nI, -1.0);
			objPosynomial.add(m);
		}
		return objPosynomial;
	}
	
	@Override
	public void compute() throws Exception {
		
		super.compute();
		
		//Compute the ideal surplus deviations (see 4.16 of Tubilla 2011)
		for (int i=0; i < params.getNumItems(); i++){
			double dI = params.getDemandRates().get(i);
			double taoI = 1.0 / params.getProductionRates().get(i);
			double rhoI = dI*taoI;
			double pcI = getCruisingFrac(i);
			double nI = getIdealFrequency(i);
			double yI = rhoI * (1 - rhoI) * (1 - pcI) / ( nI * taoI );
			idealSurplusDeviation.put(i, yI);
			log.debug(String.format("The ideal surplus deviation of item %d is %.5f", i, yI));
		}
	}

	public Double getIdealSurplusDeviation(int itemId) {
		return idealSurplusDeviation.get(itemId);
	}
	
}


