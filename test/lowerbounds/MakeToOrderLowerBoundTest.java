package lowerbounds;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import params.Params;
import params.ParamsFactory;
import util.SimBasicTest;

public class MakeToOrderLowerBoundTest extends SimBasicTest {

	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@Test
	public void test() throws Exception {
		ParamsFactory factory = new ParamsFactory("inputs/test");
		Collection<Params> paramsCollection = factory.make();
		Params params = paramsCollection.iterator().next();
		MakeToOrderLowerBound lowerBound = new MakeToOrderLowerBound("J", params);
		lowerBound.compute();
	}

}


