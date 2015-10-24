package params;

import org.junit.Test;

import util.JsonReader;
import util.SimBasicTest;

public class ParamsTest extends SimBasicTest {

	@Test
	public void testLoadingJson() throws Exception {
		Params params = JsonReader.readJsonAbsolutePath("test/resources/test_parse_params.json", Params.class);
		assertEquals("Item 0 has infinite backlog costs", Double.POSITIVE_INFINITY, params.getBacklogCosts().get(0));
	}
	
}


