package policies;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static util.UtilMethods.c;
import static util.UtilMethods.cint;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import discreteEvent.Failure;
import params.Params;
import params.Params.ParamsBuilder;
import params.PolicyParams;
import params.PolicyParams.PolicyParamsBuilder;
import processes.generators.IRandomTimeIntervalGenerator;
import sim.Clock;
import sim.Sim;
import sim.TimeInstant;
import system.Machine;

public class GallegoRecoveryPolicyTest extends AbstractPolicyTest {

    private GallegoRecoveryPolicy grpPolicy;
    private Sim sim;
    private double setup0 = 1.0;
    private double setup1 = 1.5;
    private double setup2 = 2.0;
    
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ParamsBuilder paramsBuilder = Params.builderWithDefaults();
        paramsBuilder
            .numItems(3)
            .surplusTargets(c(0.0, 0.0, 0.0))
            .initialDemand(c(0.0, 0.0, 0.0))
            .backlogCosts(c(10.0, 20.0, 30.0))
            .inventoryHoldingCosts(c(1.0, 2.0, 3.0))
            .productionRates(c(2.0, 4.0, 10.0))
            .demandRates(c(1, 1, 1))
            .setupTimes(c( setup0, setup1, setup2 ));
        
        PolicyParamsBuilder policyParamsBuilder = PolicyParams.builderWithDefaults();
        policyParamsBuilder.name("GallegoRecoveryPolicy")
                           .userDefinedProductionSequence(Optional.of(cint(1, 0, 2)));

        paramsBuilder.policyParams(policyParamsBuilder.build());

        Params params = paramsBuilder.build();
        this.sim = getSim(params);
        this.grpPolicy = (GallegoRecoveryPolicy) this.sim.getPolicy();
        this.policy = this.grpPolicy;
    }
    
    @Test
    public void testSetUp() {
        assertTrue("GRP should start with a changeover upon initialization", grpPolicy.isTimeToChangeOver() );
        assertEquals("GRP should return the first item in the sequence", sim.getMachine().getItemById(1), grpPolicy.nextItem() );
    }

    /**
     * Execute a full control cycle and assert that the sequence 1-0-2 is followed.
     */
    @Override
    public void testNextItem() {
        Machine machine = this.sim.getMachine();
        Clock clock = this.sim.getClock();
        // Production run item 1
        this.advanceUntilTime(0.0, sim, 10);
        assertTrue( machine.isChangingSetups() );
        this.advanceUntilTime( setup1, sim, 10);
        assertTrue( machine.isSetupComplete() );
        assertEquals( machine.getItemById(1), machine.getSetup() );
        this.advanceUntilTime( clock.getTime().doubleValue() + this.grpPolicy.getSprintingTimeTargetCurrentRun(), sim, 10);

        // Production run item 0
        assertTrue( machine.isChangingSetups() );
        this.advanceUntilTime(clock.getTime().doubleValue() + setup0, sim, 10);
        assertTrue( machine.isSetupComplete() );
        assertEquals( machine.getItemById(0), machine.getSetup() );
        this.advanceUntilTime( clock.getTime().doubleValue() + this.grpPolicy.getSprintingTimeTargetCurrentRun(), sim, 10);
        
        // Production run item 2
        assertTrue( machine.isChangingSetups() );
        this.advanceUntilTime(clock.getTime().doubleValue() + setup2, sim, 10);
        assertTrue( machine.isSetupComplete() );
        assertEquals( machine.getItemById(2), machine.getSetup() );
        this.advanceUntilTime( clock.getTime().doubleValue() + this.grpPolicy.getSprintingTimeTargetCurrentRun(), sim, 10);

        // Production run item 1 again (new cycle)
        assertTrue( machine.isChangingSetups() );
        this.advanceUntilTime(clock.getTime().doubleValue() + setup1, sim, 10);
        assertTrue( machine.isSetupComplete() );
        assertEquals( machine.getItemById(1), machine.getSetup() );
        this.advanceUntilTime( clock.getTime().doubleValue() + this.grpPolicy.getSprintingTimeTargetCurrentRun(), sim, 10);
    }

    /**
     * In GRP, the initially determined production time already accounts for lost time during a repair, so the
     * run length should be based on clock time and not on production time.
     */
    @Test
    public void testProductionTimeIncludesRepairTime() {
        Machine machine = this.sim.getMachine();
        Clock clock = this.sim.getClock();
        // Production run item 1
        this.advanceUntilTime(0.0, sim, 10);
        assertTrue( machine.isChangingSetups() );
        this.advanceUntilTime( setup1, sim, 10);
        assertTrue( machine.isSetupComplete() );
        assertEquals( machine.getItemById(1), machine.getSetup() );
        TimeInstant currentTime = clock.getTime();
        TimeInstant runEndTime = currentTime.add(this.grpPolicy.getSprintingTimeTargetCurrentRun());
        double deltaTime = runEndTime.subtract(currentTime).doubleValue();
        // Generate a failover at 0.1 * delta with repair of 0.5 delta
        sim.setTheRepairsGenerator( new IRandomTimeIntervalGenerator() {
            @Override
            public void warmUp(int cycles) {
            }
            @Override
            public double nextTimeInterval() {
                return 0.5 * deltaTime;
            }
        });
        sim.setTheFailuresGenerator( new IRandomTimeIntervalGenerator() {
            @Override
            public void warmUp(int cycles) {
            }
            @Override
            public double nextTimeInterval() {
                return Double.MAX_VALUE;
            }
        });
        sim.getMasterScheduler().addEvent( new Failure(currentTime.add(0.1 * deltaTime)) );
        this.advanceUntilTime( runEndTime.doubleValue(), sim, 10);
        assertTrue( "The machine should be changing setups at this point", machine.isChangingSetups() );
    }
    
    @Override
    public void testIsTargetBased() {
        assertFalse("GRP is not a target-based policy", grpPolicy.isTargetBased());
    }
    
    @Override
    public void testIsTimeToChangeOver() {
    }

    @Test
    public void testPositionsWithNegativeCorrectionGetCappedAtZero() {

        ParamsBuilder paramsBuilder = Params.builderWithDefaults();
        paramsBuilder
            .numItems(3)
            .surplusTargets(c(0.0, 0.0, 0.0))
            .initialDemand(c(0.0, 30.0, 0.0))
            .backlogCosts(c(10.0, 20.0, 30.0))
            .inventoryHoldingCosts(c(1.0, 2.0, 3.0))
            .productionRates(c(2.0, 4.0, 10.0))
            .demandRates(c(1, 1, 1))
            .setupTimes(c( setup0, setup1, setup2 ));

        PolicyParamsBuilder policyParamsBuilder = PolicyParams.builderWithDefaults();
        policyParamsBuilder.name("GallegoRecoveryPolicy")
                       .userDefinedProductionSequence(Optional.of(cint(1, 0, 1, 0, 2)));
        paramsBuilder.policyParams(policyParamsBuilder.build());
        Params params = paramsBuilder.build();

        Sim testSim = getSim(params);
        int maxEvents = 300;
        // First test position 1
        this.advanceUntilTime(16.5, testSim, maxEvents);
        assertEquals( testSim.getMachine().getItemById(0), testSim.getMachine().getSetup() );
        assertEquals("We should allocate production time to position 1",
                13.289, ( (GallegoRecoveryPolicy) testSim.getPolicy() ).getTimeRemainingCurrentRun(), 1e-2);
        // Now test position 3
        this.advanceUntilTime(33.289, testSim, maxEvents);
        assertEquals( testSim.getMachine().getItemById(0), testSim.getMachine().getSetup() );
        assertEquals("We should NOT allocate any production time to position 3",
                0, ( (GallegoRecoveryPolicy) testSim.getPolicy() ).getTimeRemainingCurrentRun(), 1e-2);
    }

}
