package params;

import org.junit.Test;

import util.JsonReader;
import util.SimBasicTest;

public class PolicyParamsTest extends SimBasicTest {

	@Test
	public void testLoadingJson() throws Exception {
		JsonReader.readJsonRelativePath("test/test_policy_params.json", PolicyParams.class);		
	}
	
}


