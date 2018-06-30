package lowerbounds;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Collection;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import params.Params;
import params.ParamsFactory;
import util.SimBasicTest;

public class MakeToOrderLowerBoundTest extends SimBasicTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    @Ignore("Ignore this test for now because it's failing due to missing files")
    public void testCompareFromFile() {
        try {
            fail("This test is failing right now because I don't have the files!");
            boolean allPassed = true;
            ParamsFactory factory = new ParamsFactory("exp_040_val");
            Collection<Params> paramsCollection = factory.make();
            BufferedReader br = new BufferedReader(new FileReader("exp_040_val/optimal_costs.txt"));
            String line;
            Map<Integer, Double> optimalCosts = Maps.newHashMap();
            boolean header = true;
            while ((line = br.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                String[] cols = line.split(" ");
                optimalCosts.put(Integer.parseInt(cols[0]), Double.parseDouble(cols[1]));
            }
            br.close();
            int i = 0;
            double maxRelError = 0.0;
            for (Params params : paramsCollection) {
                i++;
                try {
                    SurplusCostLowerBound lowerBound = new SurplusCostLowerBound("J", params);
                    lowerBound.compute();
                    double lowerBoundVal = optimalCosts.get(i);
                    double relError = Math.abs(lowerBound.getLowerBound() - lowerBoundVal) / lowerBoundVal;
                    if (relError > maxRelError) {
                        maxRelError = relError;
                    }
                    System.out.println(String.format("CASE %s J=%.5f Jval=%.5f", params.getFile(),
                            lowerBound.getLowerBound(), lowerBoundVal));

                } catch (Exception e) {
                    System.out.println(String.format("CASE %s FAILED!!!", params.getFile()));
                    allPassed = false;
                }
            }
            assertTrue("Some instances failed!", allPassed);
            System.out.println(String.format("Max relative error %.5f", maxRelError));
        } catch (Exception e) {
            fail("Could not read files " + e);
        }
    }

    @Test
    public void testIsCruisingFlag() throws Exception {
        Params params = Params.builder()
                                .demandRates(ImmutableList.of(0.1, 0.001, 0.001))
                                .setupTimes(ImmutableList.of(1.0, 100.0, 100.0))
                                .backlogCosts(ImmutableList.of(100.0, 1.0, 1.0))
                                .build();
        SurplusCostLowerBound lowerBound = new SurplusCostLowerBound("J", params);
        lowerBound.compute();
        assertTrue( "The lower bound should prescribe cruising of item 0", lowerBound.getIsCruising());
        assertTrue( "Item 0 should cruise", lowerBound.getCruisingFrac(0) > 0 );
    }

}

