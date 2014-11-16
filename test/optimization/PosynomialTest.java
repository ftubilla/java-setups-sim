package optimization;

import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.joptimizer.functions.ConvexMultivariateRealFunction;

public class PosynomialTest extends TestCase {
		
	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@Test
	public void testConstruction() {
		//Create the posynomial x^2 + 2xy
		OptimizationVar x = new OptimizationVar("x");
		OptimizationVar y = new OptimizationVar("y");
		Monomial m1 = new Monomial();
		m1.mult(x, 2.0);
		Monomial m2 = new Monomial(2.0);
		m2.mult(x, 1.0).mult(y, 1.0);
		Posynomial p = new Posynomial();
		p.add(m1).add(m2);
	
		Set<Monomial> mons = ImmutableSet.of(m1, m2);
		System.out.println(p);
		for (Monomial m : p){
			assertTrue(mons.contains(m));
		}
	}
	
	@Test
	public void testEvaluation() throws Exception {
		double tolerance = 1e-5;
		//Create the posynomial x^2 + 2xy
		OptimizationVar x = new OptimizationVar("x");
		OptimizationVar y = new OptimizationVar("y");
		Monomial m1 = new Monomial();
		m1.mult(x, 2.0);
		Monomial m2 = new Monomial(2.0);
		m2.mult(x, 1.0).mult(y, 1.0);
		Posynomial p = new Posynomial();
		p.add(m1).add(m2);
		
		Map<OptimizationVar, Double> val1 = ImmutableMap.of(x, 1.0, y, 2.0);
		assertTrue(Math.abs(p.eval(val1) - 5.0) < tolerance);				
	}
	
	@Test
	public void testPartialDiff() {
		double tolerance = 1e-5;
		//Create the posynomial x^2 + 2xy
		OptimizationVar x = new OptimizationVar("x");
		OptimizationVar y = new OptimizationVar("y");
		Monomial m1 = new Monomial();
		m1.mult(x, 2.0);
		Monomial m2 = new Monomial(2.0);
		m2.mult(x, 1.0).mult(y, 1.0);
		Posynomial p = new Posynomial();
		p.add(m1).add(m2);
		
		Map<OptimizationVar, Double> val1 = ImmutableMap.of(x, 1.0, y, 2.0);
		
		Posynomial dpdx = p.partialDiff(x);
		// 2x + 2y
		System.out.println(dpdx);
		assertTrue(Math.abs(dpdx.eval(val1) - 6.0) < tolerance);	
		Posynomial dpdy = p.partialDiff(y);
		// 2x
		System.out.println(dpdy);
		assertTrue(Math.abs(dpdy.eval(val1) - 2.0) < tolerance);
		Posynomial dpdz = p.partialDiff(new OptimizationVar("z"));
		// 0
		System.out.println(dpdz);
		assertTrue(Math.abs(dpdz.eval(val1) - 0.0) < tolerance);
	
	}

	@Test
	public void testGetConvexFunction() {
		double tolerance = 1e-6;
		// Test the posynomial x^2 + 2xy
		OptimizationVar x = new OptimizationVar("x");
		OptimizationVar y = new OptimizationVar("y");
		Monomial m1 = new Monomial();
		m1.mult(x, 2.0);
		Monomial m2 = new Monomial(2.0);
		m2.mult(x, 1.0).mult(y, 1.0);
		Posynomial p = new Posynomial();
		p.add(m1).add(m2);
		
		Map<OptimizationVar, Integer> variablesMap = ImmutableMap.of(x, 0, y, 1);
		
		ConvexMultivariateRealFunction func = p.getConvexFunction(variablesMap);
		double[] X = new double[] {1.0, 2.0};
		//Test value
		assertTrue(Math.abs(func.value(X) - 5.0) < tolerance);
		//Test gradient 2x + 2y, 2x
		double[] gradient = func.gradient(X);
		assertTrue(Math.abs(gradient[0] - 6.0) < tolerance);
		assertTrue(Math.abs(gradient[1] - 2.0) < tolerance);
		//Test Hessian 2, 2
		//             2, 0
		double[][] hessian = func.hessian(X);
		assertTrue(Math.abs(hessian[0][0] - 2.0) < tolerance);
		assertTrue(Math.abs(hessian[0][1] - 2.0) < tolerance);
		assertTrue(Math.abs(hessian[1][0] - 2.0) < tolerance);
		assertTrue(Math.abs(hessian[1][1] - 0.0) < tolerance);
	}
	
}


