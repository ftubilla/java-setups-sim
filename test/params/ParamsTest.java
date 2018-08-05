package params;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

import util.JsonReader;
import util.SimBasicTest;

public class ParamsTest extends SimBasicTest {

    @Test
    public void testLoadingJson() throws Exception {
        Params params = JsonReader.readJsonAbsolutePath("test/resources/test_parse_params.json", Params.class);
        assertEquals("Item 0 has infinite backlog costs", Double.POSITIVE_INFINITY, params.getBacklogCosts().get(0), 1e-5);
    }

    @Test
    public void testLoadingJsonWithMissingParams() throws Exception {
        Params params = JsonReader.readJsonAbsolutePath("test/resources/test_missing_params.json", Params.class);
        assertEquals( ImmutableList.of(1.0, 1.0, 1.0), params.getDemandRates() );
        assertEquals( ImmutableList.of(5.0, 5.0, 5.0), params.getProductionRates() );
        PolicyParams policyParams = params.getPolicyParams();
        assertNotNull(policyParams);
        assertEquals( PolicyParams.DEFAULT_PRIORITY_COMPARATOR, policyParams.getPriorityComparator() );
        assertEquals( PolicyParams.DEFAULT_LOWER_HEDGING_POINTS_COMPUTATION_METHOD, policyParams.getLowerHedgingPointsComputationMethod() );
    }

    @Test
    public void testMachineEfficiency() throws Exception {
        double tol = 1e-4;
        Params params = JsonReader.readJsonAbsolutePath("test/resources/test_parse_params.json", Params.class);
        params.setMeanTimeToFail(1.0);
        params.setMeanTimeToRepair(0.5);
        assertEquals(1.0 / 1.5, params.getMachineEfficiency(), tol);

        params.setMeanTimeToFail(Double.POSITIVE_INFINITY);
        params.setMeanTimeToRepair(0.5);
        assertEquals(1.0, params.getMachineEfficiency(), tol);

        params.setMeanTimeToFail(Double.POSITIVE_INFINITY);
        params.setMeanTimeToRepair(Double.POSITIVE_INFINITY);
        assertEquals(1.0, params.getMachineEfficiency(), tol);

        params.setMeanTimeToFail(1.0);
        params.setMeanTimeToRepair(Double.POSITIVE_INFINITY);
        assertEquals(0.0, params.getMachineEfficiency(), tol);

        params.setMeanTimeToFail(1.0);
        params.setMeanTimeToRepair(0.0);
        assertEquals(1.0, params.getMachineEfficiency(), tol);

        params.setMeanTimeToFail(0.0);
        params.setMeanTimeToRepair(0.0);
        assertEquals(1.0, params.getMachineEfficiency(), tol);

        params.setMeanTimeToFail(0.0);
        params.setMeanTimeToRepair(10.0);
        assertEquals(0.0, params.getMachineEfficiency(), tol);

    }

}
