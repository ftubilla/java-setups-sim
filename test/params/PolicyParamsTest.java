package params;

import org.junit.Test;

import util.JsonReader;
import util.SimBasicTest;

public class PolicyParamsTest extends SimBasicTest {

	@Test
	public void testLoadingJson() throws Exception {
		fail("This test is failing right now");
		JsonReader.readJsonRelativePath("test/test_policy_params.json", PolicyParams.class);		
	}
	
}


