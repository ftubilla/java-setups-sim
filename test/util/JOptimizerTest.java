package util;

import junit.framework.TestCase;

import org.junit.Test;

import com.joptimizer.functions.ConvexMultivariateRealFunction;
import com.joptimizer.functions.LinearMultivariateRealFunction;
import com.joptimizer.optimizers.JOptimizer;
import com.joptimizer.optimizers.OptimizationRequest;

public class JOptimizerTest extends TestCase {
	
	@Test
	public void testLP() throws Exception {
		
		// Objective function (plane)
				LinearMultivariateRealFunction objectiveFunction = new LinearMultivariateRealFunction(new double[] { -1., -1. }, 4);

				//inequalities (polyhedral feasible set G.X<H )
				ConvexMultivariateRealFunction[] inequalities = new ConvexMultivariateRealFunction[4];
				double[][] G = new double[][] {{4./3., -1}, {-1./2., 1.}, {-2., -1.}, {1./3., 1.}};
				double[] h = new double[] {2., 1./2., 2., 1./2.};
				inequalities[0] = new LinearMultivariateRealFunction(G[0], -h[0]);
				inequalities[1] = new LinearMultivariateRealFunction(G[1], -h[1]);
				inequalities[2] = new LinearMultivariateRealFunction(G[2], -h[2]);
				inequalities[3] = new LinearMultivariateRealFunction(G[3], -h[3]);
				
				//optimization problem
				OptimizationRequest or = new OptimizationRequest();
				or.setF0(objectiveFunction);
				or.setFi(inequalities);
				//or.setInitialPoint(new double[] {0.0, 0.0});//initial feasible point, not mandatory
				or.setToleranceFeas(1.E-9);
				or.setTolerance(1.E-9);
				
				//optimization
				JOptimizer opt = new JOptimizer();
				opt.setOptimizationRequest(or);
				int returnCode = opt.optimize();
				System.out.println("Return code: " + returnCode);
				
				double[] sol = opt.getOptimizationResponse().getSolution();
				System.out.println("SOL = " + sol[0] + "," + sol[1]);
				assertTrue(Math.abs(sol[0]-1.5)<1e-3);
				assertTrue(Math.abs(sol[1]-0.0)<1e-3);
				
	}
	

}


