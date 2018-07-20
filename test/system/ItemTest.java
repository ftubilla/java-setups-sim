package system;

import static org.junit.Assert.assertEquals;
import static util.UtilMethods.c;

import org.junit.Test;

import params.Params;

public class ItemTest {

    @Test
    public void test() {

        Params params = Params.builder()
                .numItems(1)
                .demandRates(c(2.0))
                .productionRates(c(4.0))
                .inventoryHoldingCosts(c(3.0))
                .backlogCosts(c(5.0))
                .setupTimes(c(10))
                .build();

        Item item = new Item(0, params);
        double tol = 1e-4;
        assertEquals( 0, item.getId() );
        assertEquals( 2.0, item.getDemandRate(), tol );
        assertEquals( 4.0, item.getProductionRate(), tol );
        assertEquals( 0.5, item.getUtilization(), tol );
        assertEquals( 3.0, item.getInventoryCostRate(), tol );
        assertEquals( 5.0, item.getBacklogCostRate(), tol );
        assertEquals( 15 / 8.0, item.getCCostRate(), tol );
    }

    @Test
    public void testCCostCalculator() {
        double tol = 1e-4;
        assertEquals( 5.0, Item.computeCCost(5.0, Double.POSITIVE_INFINITY), tol);
        assertEquals( 4.0, Item.computeCCost(Double.POSITIVE_INFINITY, 4.0), tol);
        assertEquals( Double.POSITIVE_INFINITY, Item.computeCCost(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), tol);
        assertEquals( 20 / 9.0, Item.computeCCost(5.0, 4.0), tol);
    }
    
}
