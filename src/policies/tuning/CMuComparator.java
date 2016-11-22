package policies.tuning;

import org.apache.log4j.Logger;

import com.google.common.annotations.VisibleForTesting;

import sim.Sim;
import system.Item;

/**
 * Implements the c-mu rule, where <code>c_i</code> is defined as
 * <pre>
 *      c_i = b_i * h_i / ( b_i + h_i ).
 * </pre>
 * A tolerance is used to handle cases where the coefficients for two
 * items are very close. Ties are not broken.
 * 
 * @author ftubilla
 *
 */
public class CMuComparator implements IPriorityComparator {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(CMuComparator.class);

    private double equalsTolerance;

    /**
     * Creates a new c-mu comparator, with given tolerance for treating two
     * cmu coefficients as equal.
     * 
     * @param equalsTolerance
     */
    public CMuComparator(final double equalsTolerance) {
        this.equalsTolerance = equalsTolerance;
    }

    public CMuComparator() {
        this(Sim.SURPLUS_TOLERANCE);
    }

    @Override
    public int compare(Item item1, Item item2) {

        double c1 = computeCCost(item1.getBacklogCostRate(), item1.getInventoryCostRate());
        double c2 = computeCCost(item2.getBacklogCostRate(), item2.getInventoryCostRate());

        double cmu1 = c1 * item1.getProductionRate();
        double cmu2 = c2 * item2.getProductionRate();

        // If the coefficients are within some tolerance, then treat as equal
        if ( Math.abs( ( cmu1 - cmu2 ) / cmu1 ) < this.equalsTolerance ) {
            return 0;
        } else {
            // Reverse the comparator so that highest cmu coefficients come first
            return Double.compare(cmu2, cmu1);
        }

    }

    @VisibleForTesting
    protected double computeCCost(final double b, final double h) {
        if ( Double.isFinite(b) && Double.isFinite(h) ) {
            return b * h / ( b + h );
        } else {
            if ( Double.isFinite(b) ) {
                return b;
            } else {
                if ( Double.isFinite(h) ) {
                    return h;
                } else {
                    return Double.POSITIVE_INFINITY;
                }
            }
        }
    }

}
