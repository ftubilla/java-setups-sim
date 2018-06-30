package policies;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;

import discreteEvent.ControlEvent;
import discreteEvent.SurplusControlEvent;
import lombok.extern.apachecommons.CommonsLog;
import sim.Sim;
import system.Item;

/**
 * The idea behind this policy is to take advantage of the fact that the HZP
 * performs best when the utilization of all items are roughly equal, and also
 * the fact that recovery from a long failure is best if the oscillations for
 * the costly items have an upper envelope that's convex (i.e., we have a large
 * initial inventory accumulation and then we progressively lower the
 * inventory).
 * 
 * The idea behind this policy then is to simulate a larger utilization (rho)
 * for items with low utilization, by over producing beyond the target surplus;
 * similarly, a lower utilization is simulated by under producing. The end
 * effect is that items with low utilization exceed their upper hedging point
 * during a long recovery, while items with high utilization undershoot it.
 * 
 * This policy implementation is deprecated because it has not been tested thoroughly.
 * 
 * @author ftubilla
 *
 */
@CommonsLog
@Deprecated
public class HedgingZonePolicyWithLoadBalance extends HedgingZonePolicy {

    private Map<Item, Double> productionTimeCorrectionFactor = Maps.newHashMap();
    private Map<Item, Double> latestTarget = Maps.newHashMap();
    private Double targetOfCurrentSetup = null;

    @Override
    public void setUpPolicy(final Sim sim) {

        super.setUpPolicy(sim);

        // Compute the production time correction factors (these inflate/deflate the time to produce each item)
        double averageUtil = StreamSupport.stream(sim.getMachine().spliterator(), false)
                .mapToDouble(i -> i.getDemandRate() / i.getProductionRate()).average().getAsDouble();

        for ( Item item : sim.getMachine() ) {
            double itemUtil = item.getDemandRate() / item.getProductionRate();
            double correctionFactor = ( 1 - itemUtil ) / ( 1 - averageUtil );
            log.debug(String.format("The utilization of %s is %.3f, and the average util %.3f. The correction factor is %.3f",
                    item, itemUtil, averageUtil, correctionFactor));
            this.productionTimeCorrectionFactor.put(item, correctionFactor);
            this.latestTarget.put(item, item.getSurplusTarget());
        }

    }

    @Override
    protected boolean isTimeToChangeOver() {
        if ( isInTheFractionalHedgingZone( this.cruisingParameter ) ) {
            //Changeovers never occur in the fractional hedging zone (TODO check this behavior)
            return false;
        } else {
           if ( this.targetOfCurrentSetup != null ) {
            double currentSurplus = this.currentSetup.getSurplus();
            boolean isTimeToChangeOver = currentSurplus >= this.targetOfCurrentSetup - Sim.SURPLUS_TOLERANCE;
            log.trace(String.format("Current setup %s surplus %.3f >= target surplus %.3f ? %s", this.currentSetup,
                    currentSurplus, this.targetOfCurrentSetup, isTimeToChangeOver));
            return isTimeToChangeOver;
           }
        }
        return false;
    }

    @Override
    protected ControlEvent onReady() {
        super.onReady(); // ignore the control event that's returned
        if ( this.targetOfCurrentSetup == null ) {
            // First time producing this item, compute its new target
            double currentSetupTarget = this.currentSetup.getSurplusTarget();
            double timeToReachTarget = this.currentSetup.getFluidTimeToSurplusLevel(currentSetupTarget);
            double correctedTime = this.productionTimeCorrectionFactor.get(this.currentSetup) * timeToReachTarget;
            // The new target would be the value of the surplus after producing for correctedTime units
            this.targetOfCurrentSetup = correctedTime * ( this.currentSetup.getProductionRate() - this.currentSetup.getDemandRate() ) +
                    this.currentSetup.getSurplus();
            // Update the target for this item
            this.latestTarget.put(this.currentSetup, this.targetOfCurrentSetup);
            log.debug(String.format("Setting the target surplus for current setup %s to %.2f" +
                    " (original target is %.2f, surplus %.2f, time to reach %.2f)",
                        this.currentSetup, this.targetOfCurrentSetup, this.currentSetup.getSurplusTarget(),
                            this.currentSetup.getSurplus(), timeToReachTarget));
        }
        return new SurplusControlEvent(this.currentSetup, this.targetOfCurrentSetup, this.clock.getTime(), this.hasDiscreteMaterial);
    }

    @Override
    protected Item nextItem() {
        // get the next item directly from the HZP
        Item nextItem = super.nextItem();
        // reset the target for the current setup (this needs to happen AFTER getting the next item TODO)
        this.targetOfCurrentSetup = null;
        return nextItem;
    }

    @Override
    public boolean isTargetBased() {
        return false;
    }

    @VisibleForTesting
    protected boolean isInTheFractionalHedgingZone( final double fraction, final Item item ) {
//        double target = item.getSurplusTarget();
//        if ( this.currentSetup != null && this.targetOfCurrentSetup != null && this.currentSetup.equals(item) ) {
//            target = this.targetOfCurrentSetup;
//        }
        double target = this.latestTarget.get(item);
        // We are using the latest target AND the lower hedging point, so it's a bigger DZ than originally
        double deltaZ = Math.max(target, item.getSurplusTarget()) - this.lowerHedgingPoints.getLowerHedgingPoint(item);
        boolean isInTheFractionalHZ = target - item.getSurplus() <= fraction * deltaZ;
        log.trace(String.format("%s has: latest target: %.2f delta_z: %.2f surplus: %.2f zone fraction %.2f. In the fractional hedging zone ? %s",
                item, target, deltaZ, item.getSurplus(), fraction, isInTheFractionalHZ));
        return isInTheFractionalHZ;
    }

    @Override
    @VisibleForTesting
    protected double computeTimeToExitFractionalHedgingZone(final double fraction) {
        double minExitTime = Double.MAX_VALUE;
        for (Item item : this.sortedItems) {
            double zu = this.latestTarget.get(item);
            double zl = this.lowerHedgingPoints.getLowerHedgingPoint(item);
            double surplusBoundary = zu - fraction * ( zu - zl );
            double exitTime = item.getFluidTimeToSurplusLevel( surplusBoundary );
            assert exitTime >= 0 : "The system is not in the hedging zone!";
            if (exitTime < minExitTime) {
                minExitTime = exitTime;
            }
        }
        return minExitTime;
    }

    protected Optional<Item> selectItemFromReadySet(final Set<Item> readyItems) {
        Optional<Pair<Item, Double>> maximizingPairOpt = 
                readyItems.stream()
                .map( item -> Pair.of(item,
                   (this.latestTarget.get(item) - item.getSurplus()) / ( Math.max(item.getSurplusTarget(), this.latestTarget.get(item)) - this.lowerHedgingPoints.getLowerHedgingPoint(item) ) ) )
                .max( Comparator.comparingDouble( ( Pair<Item, Double> pair ) -> pair.getRight() ) );
        Item returnItem = null;
        if ( maximizingPairOpt.isPresent() ) {
            Pair<Item, Double> maximizingPair = maximizingPairOpt.get();
            log.trace(String.format("Item %s had the largest ratio of %.5f", maximizingPair.getLeft(), maximizingPair.getRight()));
            returnItem = maximizingPair.getLeft();
        }
        return Optional.ofNullable(returnItem);
    }
    
}

