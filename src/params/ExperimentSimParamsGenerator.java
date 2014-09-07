package params;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import util.JsonReader;

import com.google.common.collect.ImmutableList;

import experiment.ExperimentParams;
import experiment.ParameterCombo;

public class ExperimentSimParamsGenerator {
	private static Logger logger = Logger.getLogger(ExperimentSimParamsGenerator.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();

	@SuppressWarnings("unused")
	private boolean trace = logger.isTraceEnabled();
	
	public static List<Params> generate(ExperimentParams expParams){
		
		List<Params> simParamsList = new ArrayList<Params>();
		

		String[] policies = new String[]{"ClearTheLargestDeviationPolicy","RoundRobinPolicy"};
		
		for (int pol = 0; pol < policies.length; pol++) {
			long seed = 0L;
			for (int i = 0; i < expParams.getReplicates(); i++) {
				for (ParameterCombo demandRatesCombo : expParams.getDemandRatesCombos()) {
					for (ParameterCombo setupTimesCombo : expParams.getSetupTimesCombos()) {

						Params params = JsonReader.readJson(expParams.getBaseJson() + ".json", Params.class);

						List<Double> demandRates = new ArrayList<Double>(expParams.getNumItems());
						List<Double> setupTimes = new ArrayList<Double>(expParams.getNumItems());
						for (int item = 0; item < expParams.getNumItems(); item++) {
							demandRates.add(item, demandRatesCombo.get(item));
							setupTimes.add(item, setupTimesCombo.get(item));
						}

						params.setDemandRates(ImmutableList.copyOf(demandRates));
						params.setSetupTimes(ImmutableList.copyOf(setupTimes));
						params.setSeed(seed++);
						PolicyParams policyParams = params.getPolicyParams();
						policyParams.setName(policies[pol]);
						params.setPolicyParams(policyParams);
						
						// VERY important to avoid large files!
						params.setRecordHighFreq(false);

						simParamsList.add(params);

					}
				}
			}
		}
								
		return simParamsList;
		
	}
	
	
	
}


