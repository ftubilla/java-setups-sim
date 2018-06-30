package params;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import util.JsonReader;
import util.SimBasicTest;

public class ParamsTest extends SimBasicTest {

    @Test
    public void testLoadingJson() throws Exception {
        Params params = JsonReader.readJsonAbsolutePath("test/resources/test_parse_params.json", Params.class);
        assertEquals("Item 0 has infinite backlog costs", Double.POSITIVE_INFINITY, params.getBacklogCosts().get(0), 1e-5);
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
