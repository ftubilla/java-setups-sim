package sequences;

import static util.UtilMethods.c;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import params.Params;
import params.Params.ParamsBuilder;
import sequences.OptimalSequenceFinder.SearchNode;
import system.Item;
import util.SimBasicTest;

public class OptimalSequenceFinderTest extends SimBasicTest {

    @Test
    public void testSearchNode() {

        ParamsBuilder paramsBuilder = Params.builderWithDefaults();
        paramsBuilder
            .numItems(3)
            .surplusTargets(c(0.0, 0.0, 0.0))
            .initialDemand(c(0.0, 20.0, 30.0))
            .backlogCosts(c( 19.,  19.,  19. ))
            .inventoryHoldingCosts(c( 1.0, 1.0, 1.0 ))
            .productionRates(c( 0.36630037,  0.36630037,  0.36630037 ))
            .demandRates(c( 0.03333333,  0.13333333,  0.13333333 ))
            .setupTimes(c( 7.77777778,  1.11111111,  1.11111111 ));

        Params params = paramsBuilder.build();
        Item item0 = new Item(0, params);
        Item item1 = new Item(1, params);
        Item item2 = new Item(2, params);

        SearchNode node = new OptimalSequenceFinder.SearchNode(item0, item1);
        assertFalse( node.containsAllItems(Sets.newHashSet(item0, item1, item2) ) );
        assertTrue( node.canAppend(item2) );
        assertTrue( node.canAppend(item0) );
        assertFalse( node.canAppend(item1) );
        assertEquals(2, node.getSize());
        SearchNode branchNode = node.append(item2);
        assertTrue( branchNode.containsAllItems(Sets.newHashSet(item0, item1, item2)));
        ProductionSequence sequence = branchNode.getProductionSequence();
        assertEquals( new ProductionSequence(item0, item1, item2), sequence);
        assertTrue( branchNode.endPointsDiffer() );
        assertFalse( branchNode.append(item0).endPointsDiffer() );
        
        SearchNode emptyNode = new OptimalSequenceFinder.SearchNode();
        assertTrue(emptyNode.canAppend(item0));
        assertFalse(emptyNode.endPointsDiffer());
    }

    @Test
    public void testFinder() throws Exception {

        ParamsBuilder paramsBuilder = Params.builderWithDefaults();
        paramsBuilder
            .numItems(3)
            .surplusTargets(c(0.0, 0.0, 0.0))
            .initialDemand(c(0.0, 20.0, 30.0))
            .backlogCosts(c( 19.,  19.,  19. ))
            .inventoryHoldingCosts(c( 1.0, 1.0, 1.0 ))
            .productionRates(c( 0.36630037,  0.36630037,  0.36630037 ))
            .demandRates(c( 0.03333333,  0.13333333,  0.13333333 ))
            .setupTimes(c( 7.77777778,  1.11111111,  1.11111111 ));

        Params params = paramsBuilder.build();
        Item item0 = new Item(0, params);
        Item item1 = new Item(1, params);
        Item item2 = new Item(2, params);
        double tol = 1e-4;
        OptimalSequenceFinder finder = new OptimalSequenceFinder( Lists.newArrayList(item0, item1, item2), 0.91);
        OptimalFCyclicSchedule schedule = finder.find(12);
        assertEquals( 6.23855105, schedule.getScheduleCost(), tol);

    }
}
