package policies;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import lombok.Data;
import lombok.ToString;
import lombok.extern.apachecommons.CommonsLog;
import policies.GRPControlCycle.GRPRunInfo;
import sequences.ProductionSequence;
import system.Item;
import system.Machine;

/**
 * A handy class for holding the information on the control cycle for
 * {@link GallegoRecoveryPolicy}. A control cycle contains the sequence of items
 * that will be produced, and the production run length. If the control law
 * gives a negative run time for some item <code>i</code>, then the run time of
 * <i>all</i> positions in the sequence where item <code>i</code> occurs are
 * capped at 0. This prevents instabilities where we could skip the item in one
 * position but still produce it in some other position with non-negative run
 * time. Also, note that for simplicity we still switch over to the item even if
 * it has 0 run time; this ensures that the sequence is always valid and adds
 * some idleness.
 * 
 * @author ftubilla
 *
 */
@CommonsLog
@ToString
public class GRPControlCycle implements Iterator<GRPRunInfo> {

    private final double[] runDuration;             // The duration of the run at each position of the sequence, after correcting with the control
    private final ProductionSequence sequence;
    private Integer currentPosition = null;         // The current position in the cycle's sequence

    /**
     * Constructs a new control cycle instance, given the current state of the machine and the static info about the GRP.
     *  
     * @param sequence
     * @param surplusTarget
     * @param sprintingTimeTarget
     * @param gainMatrix
     * @param machine
     */
    public GRPControlCycle( final Machine machine, final ProductionSequence sequence, final Map<Item, Double> surplusTarget,
            final double[] sprintingTimeTarget, final double[][] gainMatrix ) {

        log.trace("New control cycle. Updating correction vector.");
        this.runDuration = new double[sequence.getSize()];
        this.sequence = sequence;
        int n = machine.getNumItems();
        double[] error = new double[n];

        // Compute the error
        for ( int j = 0; j < n; j++ ) {
            Item item = machine.getItemById(j);
            error[j] = surplusTarget.get(item) - item.getSurplus();
            log.trace(String.format("%s has a surplus error of %.2f (initial surplus target %.2f - current surplus %.2f)",
                item, error[j], surplusTarget.get(item), item.getSurplus()));
        }

        Map<Item, Double> netTotalRunTime = new HashMap<>();
        // Compute the control
        for ( int i = 0; i < this.sequence.getSize(); i++ ) {
            double correction = 0.0;
            for ( int j = 0; j < n; j++ ) {
                correction += gainMatrix[i][j] * error[j];
            }
            double runDuration = sprintingTimeTarget[i] + correction;
            Item item = this.sequence.getItemAtPosition(i);
            netTotalRunTime.merge(item, runDuration, Double::sum);
            if ( runDuration < 0 ) {
                log.trace(String.format("Item %s has negative production time! All of its positions will be set to 0 run time", item));
            }
            this.runDuration[i] = Math.max( 0, runDuration );
            log.trace(String.format("The production time correction at position %d (%s) is %.2f. Updated sprinting time = %.2f",
                i, item, correction, this.runDuration[i]));
        }

        // If the net run duration for an item is < 0, set all its positions to 0 (otw the system can become unstable)
        for ( int i = 0; i < this.sequence.getSize(); i++ ) {
            Item item = this.sequence.getItemAtPosition(i);
            if ( netTotalRunTime.get(item) < 0 ) {
                log.trace(String.format("Setting to 0 the run time for %s at position %d because the item has a net negative total run duration",
                        item, i));
                this.runDuration[i] = 0;
            }
        }

}

    /**
     * Returns <code>true</code> if the cycle is valid and the current position is not the last position in the sequence.
     */
    @Override
    public boolean hasNext() {
        return this.currentPosition == null || this.currentPosition < this.sequence.getSize() - 1;
    }

    @Override
    public GRPRunInfo next() {
        if ( hasNext() ) {
            // Update the current position
            if ( this.currentPosition == null ) {
                this.currentPosition = 0;
            } else {
                this.currentPosition++;
            }
            return new GRPRunInfo(this.sequence.getItemAtPosition(this.currentPosition), 
                    this.runDuration[this.currentPosition]);
        } else {
            log.trace(String.format("The control cycle with current position %d has no next run", this.currentPosition));
            throw new NoSuchElementException("No more GRP runs!");
        }
    }

    /**
     * Information about the current run in the control cycle.
     */
    @Data
    public static class GRPRunInfo {
        private final Item item;
        private final double runDuration;
    }

}
