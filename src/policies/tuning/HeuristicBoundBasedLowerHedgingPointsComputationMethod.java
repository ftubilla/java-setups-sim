package policies.tuning;

import java.util.Map;

import com.google.common.collect.Maps;

import lombok.extern.apachecommons.CommonsLog;
import lowerbounds.FrequencyRatioLimitedSurplusCostHeuristicBound;
import sim.Sim;
import system.Item;

/**
 * Computes the lower hedging point based on the heuristic bound (see {@link FrequencyRatioLimitedSurplusCostHeuristicBound}).
 * 
 * @author ftubilla
 *
 */
@CommonsLog
public class HeuristicBoundBasedLowerHedgingPointsComputationMethod implements ILowerHedgingPointsComputationMethod {

    private Map<Item, Double> lowerHedgingPoints = Maps.newHashMap();

    @Override
    public void compute(Sim sim) {
        if (sim.getParams().getPolicyParams().getUserDefinedLowerHedgingPoints().isPresent()) {
            log.warn("Warning. Lower hedging points were given in the params, but they will be overriden.");
        }
        FrequencyRatioLimitedSurplusCostHeuristicBound heuristicBound =
                new FrequencyRatioLimitedSurplusCostHeuristicBound("heuristic bound", sim.getParams());
        try {
            heuristicBound.compute();
        } catch (Exception e){
            log.error("Could not compute heuristic bound", e);
            throw new RuntimeException(e);
        }
        for (Item item : sim.getMachine()) {
            double y = heuristicBound.getIdealSurplusDeviation(item.getId());
            double S = item.getSetupTime();
            double d = item.getDemandRate();
            double dZ = y - S * d;
            double lowerHP = item.getSurplusTarget() - dZ;
            log.debug(String.format("Setting the lower hedging point for %s to %.3f", item, lowerHP));
            lowerHedgingPoints.put(item, lowerHP);
        }
    }

    @Override
    public double getLowerHedgingPoint(Item item) {
        return lowerHedgingPoints.get(item);
    }

}
