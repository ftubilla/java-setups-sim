package lowerbounds;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Collection;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import params.Params;
import params.ParamsFactory;
import util.SimBasicTest;

import com.google.common.collect.Maps;

public class MakeToOrderLowerBoundTest extends SimBasicTest {

	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@Test
	public void test() throws Exception {
		boolean allPassed = true;
		ParamsFactory factory = new ParamsFactory("exp_040_val");
		Collection<Params> paramsCollection = factory.make();
		BufferedReader br = new BufferedReader(new FileReader("exp_040_val/optimal_costs.txt"));
		String line;
		Map<Integer, Double> optimalCosts = Maps.newHashMap();
		boolean header = true;
		while ((line = br.readLine()) != null) {
		   if (header){
			   header = false;
			   continue;
		   }
			String[] cols = line.split(" ");
		   optimalCosts.put(Integer.parseInt(cols[0]), Double.parseDouble(cols[1]));
		}
		br.close();
		int i = 0;
		double maxRelError = 0.0;
		for (Params params : paramsCollection){			
			i++;
			try {
				MakeToOrderLowerBound lowerBound = new MakeToOrderLowerBound("J", params);
				lowerBound.compute();
				double lowerBoundVal = optimalCosts.get(i);
				double relError = Math.abs(lowerBound.getLowerBound() - lowerBoundVal) / lowerBoundVal;
				if (relError > maxRelError) {
					maxRelError = relError;
				}
				System.out.println(String.format("CASE %s J=%.5f Jval=%.5f",
						params.getFile(), lowerBound.getLowerBound(), lowerBoundVal));
				
			} catch (Exception e) {
				System.out.println(String.format("CASE %s FAILED!!!", params.getFile()));
				allPassed = false;
			}
		}
		assertTrue("Some instances failed!", allPassed);
		System.out.println(String.format("Max relative error %.5f", maxRelError));
	}

}


