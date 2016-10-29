package policies.tuning;

import org.apache.log4j.Logger;


import system.Item;

/**
 * Implements the c-mu rule. Assumes that holding costs and backlog costs hold
 * the same proportion (i.e., h_i/b_i = Constant for all i). Ties are not broken!
 * 
 * @author ftubilla
 *
 */
public class CMuComparator implements IPriorityComparator {

    @SuppressWarnings("unused")
    private static Logger logger = Logger.getLogger(CMuComparator.class);

    @Override
    public int compare(Item item1, Item item2) {

        double ratio1 = item1.getInventoryCostRate() / item1.getBacklogCostRate();
        double ratio2 = item2.getInventoryCostRate() / item2.getBacklogCostRate();
        assert Math.abs(
                ratio1 - ratio2) < 1e-4 : "C-Mu comparator only works with constant inventory and backlog costs ratios";

        double cmu1 = item1.getBacklogCostRate() * item1.getProductionRate();
        double cmu2 = item2.getBacklogCostRate() * item2.getProductionRate();

        // Reverse the comparator so that highest cmu coefficients come first
        return Double.compare(cmu2, cmu1);

    }
}
