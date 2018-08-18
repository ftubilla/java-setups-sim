package policies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static util.UtilMethods.c;

import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Maps;

import params.Params;
import sim.Sim;
import system.Item;
import system.Machine;

public class LinearHedgingZonePolicyTest extends AbstractPolicyTest {

    private LinearHedgingZonePolicy linearHZP;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.linearHZP = new LinearHedgingZonePolicy();
        this.policy = this.linearHZP;
    }

    @Test
    public void testFactors() {
        Params params = Params.builder()
                .numItems(2)
                .demandRates(c(2.0, 3.0))
                .productionRates(c(4.0, 9.0))
                .inventoryHoldingCosts(c(3.0, 1.0))
                .backlogCosts(c(5.0, 2.0))
                .setupTimes(c(10, 20))
                .build();

        Sim sim = getSim(params);
        Machine machine = sim.getMachine();
        Map<Item, Double> factors = LinearHedgingZonePolicy.computeHedgingPointFactors(machine);
        assertEquals( 2, factors.size() );
        double tol = 1e-4;
        Item item0 = machine.getItemById(0);
        Item item1 = machine.getItemById(1);
        double alpha = 0.5 * ( item0.getCCostRate() * 4.0 / 0.5 + item1.getCCostRate() * 9.0 / ( 1 / 3.0 ) );
        assertEquals( item0.getDemandRate() / ( 2 - 1 ) * ( (4.0 - 2.0 ) / ( Math.sqrt( alpha * 2.0 / item0.getCCostRate() ) - 2.0 ) - 1 ),
                factors.get(item0), tol );
    }

    @Test
    public void testExpansionFactors() {
        Params params = Params.builder()
                .numItems(2)
                .demandRates(c(2.0, 3.0))
                .productionRates(c(4.0, 9.0))
                .inventoryHoldingCosts(c(3.0, 1.0))
                .backlogCosts(c(5.0, 2.0))
                .setupTimes(c(10, 20))
                .build();
        Sim sim = getSim(params);
        Machine machine = sim.getMachine();
        double tol = 1e-4;
        Item item0 = machine.getItemById(0);
        Item item1 = machine.getItemById(1);
        // Calculate some fictitious upper hedging point factors
        Map<Item, Double> factors = Maps.newHashMap();
        factors.put( item0, 5.0 );
        factors.put( item1, 6.0 );
        Map<Item, Double> expansionFactors = LinearHedgingZonePolicy.computeHedgingZoneExpansionFactors(factors);
        // Note that the formula is sqrt(1 + factor[i]^2 / d[j]^2)
        assertEquals( Math.sqrt( 1 + 25 / 9.0 ), expansionFactors.get(item0), tol );
        assertEquals( Math.sqrt( 1 + 36 / 4.0 ), expansionFactors.get(item1), tol );
    }

    @Test
    public void testComputeHedgingPointIncrementSameCmuRhoRatio() {
        // Items have the same cmu/rho ratio of 1 / 0.25
        Params params = Params.builder()
                .numItems(2)
                .demandRates(c(2.0, 3.0))
                .productionRates(c(8.0, 12.0))
                .inventoryHoldingCosts(c(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY))
                .backlogCosts(c(1/8.0, 1/12.0))
                .setupTimes(c(10, 20))
                .build();

        Sim sim = getSim(params);
        this.policy.setUpPolicy(sim);

        double tol = 1e-4;
        assertEquals( 0, this.linearHZP.computeHedgingPointIncrement(sim.getMachine().getItemById(0)), tol);
        assertEquals( 0, this.linearHZP.computeHedgingPointIncrementAtTimeDelta(234, sim.getMachine().getItemById(0)), tol);
        assertEquals( 0, this.linearHZP.computeHedgingPointIncrement(sim.getMachine().getItemById(1)), tol);
        assertEquals( 0, this.linearHZP.computeHedgingPointIncrementAtTimeDelta(234, sim.getMachine().getItemById(1)), tol);
    }

    @Test
    public void testComputeHedgingPointIncrementDifferentCmuRhoRatio() {
        // Item 0 has 2x the cmu/rho ratio of item 1. We should have a linearly incrementing upper hedging point for 0 and decrementing for 1
        // we assume that the system starts at the nominal upper hedging point
        Params params = Params.builder()
                .numItems(2)
                .demandRates(c(1.0, 1.0))
                .productionRates(c(4.0, 2.0))
                .inventoryHoldingCosts(c(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY))
                .backlogCosts(c(1, 1))
                .setupTimes(c(10, 10))
                .surplusTargets(c(0, 0))
                .initialDemand(c(0, 0))
                .build();

        Sim sim = getSim(params);
        this.policy.setUpPolicy(sim);

        double tol = 1e-4;
        Item item0 = sim.getMachine().getItemById(0);
        Item item1 = sim.getMachine().getItemById(1);
        // At time 0, the hedging point corresponds to the nominal hedging point
        assertEquals( 0, this.linearHZP.computeHedgingPointIncrement(item0), tol);
        assertEquals( 0, this.linearHZP.computeHedgingPointIncrement(item1), tol);
        // Now check at time 10
        double zU0_10 = this.linearHZP.computeHedgingPointIncrementAtTimeDelta(10, item0);
        double zU1_10 = this.linearHZP.computeHedgingPointIncrementAtTimeDelta(10, item1);
        assertTrue( zU0_10 > 0 );
        assertTrue( zU1_10 < 0 );
        // Now check at time 20 and ensure that we have a straight line
        double zU0_20 = this.linearHZP.computeHedgingPointIncrementAtTimeDelta(20, item0);
        double zU1_20 = this.linearHZP.computeHedgingPointIncrementAtTimeDelta(20, item1);
        assertEquals( ( zU0_20 - zU0_10 ) / 10.0, ( zU0_10 - 0 ) / 10.0, tol ); 
        assertEquals( ( zU1_20 - zU1_10 ) / 10.0, ( zU1_10 - 0 ) / 10.0, tol ); 
    }

    @Override
    public void testIsTargetBased() {
        assertFalse( this.policy.isTargetBased() );
    }

    @Override
    public void testNextItem() {
        // TODO Implement this test
    }

}
