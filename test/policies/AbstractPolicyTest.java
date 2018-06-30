package policies;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static util.UtilMethods.c;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import discreteEvent.ControlEvent;
import lombok.extern.apachecommons.CommonsLog;
import output.Recorders;
import params.Params;
import params.Params.ParamsBuilder;
import params.PolicyParams;
import params.PolicyParams.PolicyParamsBuilder;
import sim.Sim;
import sim.SimSetup;
import util.SimBasicTest;

@CommonsLog
public abstract class AbstractPolicyTest extends SimBasicTest {

    protected AbstractPolicy policy;

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testPolicyNotNull() {
        assertFalse("Initialize the policy in setUp()", policy == null);
    }

    @Test
    public void testIsTimeToChangeOver() {
        // Create a two-item system with the first item below its target
        PolicyParamsBuilder policyParamsBuilder = PolicyParams.builderWithDefaults();
        policyParamsBuilder.name(policy.getClass().getSimpleName());

        ParamsBuilder paramsBuilder = Params.builderWithDefaults();
        paramsBuilder
            .numItems(2)
            .initialSetup(0)
            .demandRates(c(0.1, 0.1))
            .productionRates(c(1.0, 1.0))
            .setupTimes(c(1.0, 1.0))
            .surplusTargets(c(10.0, 10.0))
            .initialDemand(c(0.0, 0.0))
            .inventoryHoldingCosts(c(1.0, 1.0))
            .backlogCosts(c(1.0, 1.0))
            .policyParams( policyParamsBuilder.build() );
        
        Sim sim = new Sim(paramsBuilder.build());
        SimSetup.setup(sim, new Recorders());
        policy.setUpPolicy(sim);
        assertFalse("The item is still below target", policy.isTimeToChangeOver());

        // Make sure that if it's not time to changeover, we return null
        assertNull("If it's not time to change over, nextItem() should return null", policy.nextItem());

    }

    @Test
    public abstract void testNextItem();

    @Test
    public abstract void testIsTargetBased();

    /**
     * Creates and sets up a new sim instance for testing.
     * 
     * @param params
     * @return Sim
     */
    protected Sim getSim(Params params) {
        Sim sim = new Sim(params);
        SimSetup.setup(sim, new Recorders());
        return sim;
    }

    protected void advanceUntilTime(double time, Sim sim, int maxEvents) {
        log.debug(String.format("Advancing sim to time %.2f. Current time %.2f", time, sim.getClock().getTime()));
        // Add a control event at current time if there are no events in the sim (typically at startup)
        if ( sim.getMasterScheduler().nextEventTime() >= Double.MAX_VALUE ) {
            log.warn("No more events in sim. Adding a control event at the current time to jump start the sim");
            sim.getMasterScheduler().addEvent( new ControlEvent(sim.getTime()) );
        }
        int events = 0;
        while ( events < maxEvents ) {
            double nextEventTime = sim.getMasterScheduler().nextEventTime();
            if ( nextEventTime <= time ) {
                log.trace(String.format("Next event occurs at time %.2f. Executing it", nextEventTime));
                sim.getMasterScheduler().getNextEvent().handle(sim);
                events++;
            } else {
                log.trace(String.format("Next event occurs at time %.2f, after the desired time. Stopping.", nextEventTime));
                break;
            }
        }
        if ( events == maxEvents ) {
            throw new RuntimeException(String.format("Reached the maximum number of allowed events (%d). Current sim time %.2f",
                    events, sim.getClock().getTime()));
        }
    }

    /**
     * Fills the remaining mocked params
     * 
     * @param params
     */
    @Deprecated
    protected void fillRemainingMockedParams(Params params) {
        int numItems = params.getNumItems();
        List<Double> onesDouble = new ArrayList<Double>();
        List<Double> zerosDouble = new ArrayList<Double>();
        List<Double> oneOverNumItemsDouble = new ArrayList<Double>();
        for (int i = 0; i < numItems; i++) {
            onesDouble.add(1.0);
            zerosDouble.add(0.0);
            oneOverNumItemsDouble.add(1.0 / numItems);
        }
        if (params.getBacklogCosts() == null || params.getBacklogCosts().isEmpty()) {
            when(params.getBacklogCosts()).thenReturn(ImmutableList.copyOf(onesDouble));
        }
        if (params.getInventoryHoldingCosts() == null || params.getInventoryHoldingCosts().isEmpty()) {
            when(params.getInventoryHoldingCosts()).thenReturn(ImmutableList.copyOf(onesDouble));
        }
        if (params.getInitialDemand() == null || params.getInitialDemand().isEmpty()) {
            when(params.getInitialDemand()).thenReturn(ImmutableList.copyOf(zerosDouble));
        }
        if (params.getProductionRates() == null || params.getProductionRates().isEmpty()) {
            when(params.getProductionRates()).thenReturn(ImmutableList.copyOf(onesDouble));
        }
        if (params.getDemandRates() == null || params.getDemandRates().isEmpty()) {
            when(params.getDemandRates()).thenReturn(ImmutableList.copyOf(oneOverNumItemsDouble));
        }
        if (params.getSetupTimes() == null || params.getSetupTimes().isEmpty()) {
            when(params.getSetupTimes()).thenReturn(ImmutableList.copyOf(onesDouble));
        }
        when(params.getMachineEfficiency()).thenCallRealMethod();

        if (params.getPolicyParams() == null) {
            when(params.getPolicyParams()).thenReturn(mock(PolicyParams.class));
        }
        PolicyParams policyParams = params.getPolicyParams();
        if (policyParams.getLowerHedgingPointsComputationMethod() == null) {
            when(policyParams.getLowerHedgingPointsComputationMethod())
                    .thenReturn(PolicyParams.DEFAULT_LOWER_HEDGING_POINTS_COMPUTATION_METHOD);
        }
        if (policyParams.getPriorityComparator() == null) {
            when(policyParams.getPriorityComparator()).thenReturn(PolicyParams.DEFAULT_PRIORITY_COMPARATOR);
        }
        if (policyParams.getUserDefinedLowerHedgingPoints() == null) {
            Optional<ImmutableList<Double>> x = Optional.empty();
            when(policyParams.getUserDefinedLowerHedgingPoints()).thenReturn(x);
        }
        if (policyParams.getUserDefinedIsCruising() == null) {
            Optional<Boolean> x = Optional.empty();
            when(policyParams.getUserDefinedIsCruising()).thenReturn(x);
        }

    }

}
