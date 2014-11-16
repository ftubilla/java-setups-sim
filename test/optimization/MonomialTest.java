package optimization;

import java.util.Map;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class MonomialTest extends TestCase {

	private OptimizationVar x = new OptimizationVar("x");
	private OptimizationVar y = new OptimizationVar("y");
	private OptimizationVar z = new OptimizationVar("z");
	private OptimizationVar w = new OptimizationVar("w");
	
	
	@Before
	public void setUp() throws Exception {
		super.setUp();		
	}
	
	@Test
	public void testConstruction() {
		//Create the term 2 x^2 y^3 / z		
		Monomial mon = new Monomial(1.0);
		mon.mult(x, 2).mult(y, 3).mult(z, -1).mult(2.0);
		System.out.println(mon);
		assertTrue(mon.getCoefficient() == 2.0);
		assertTrue(mon.getExponent(x) == 2.0);
		assertTrue(mon.getExponent(y) == 3.0);
		assertTrue(mon.getExponent(z) == -1.0);
		assertTrue(mon.getExponent(w) == 0.0);
	}
	
	@Test
	public void testEvaluation() throws Exception {
		double tolerance = 1e-5;
		Monomial mon = new Monomial(1.0);
		//Create the term 2 x^2 y^3 / z
		mon.mult(x, 2).mult(y, 3).mult(z, -1).mult(2.0);
		Map<OptimizationVar, Double> val1 = ImmutableMap.of(x,1.0,y,2.0,z,2.0);				
		assertTrue(Math.abs(mon.eval(val1)-8) < tolerance);
		Map<OptimizationVar, Double> val2 = ImmutableMap.of(x,0.0,y,2.0,z,2.0);				
		assertTrue(Math.abs(mon.eval(val2)-0) < tolerance);
		Map<OptimizationVar, Double> val3 = ImmutableMap.of(x,1.0,y,1.0,z,1.0);				
		assertTrue(Math.abs(mon.eval(val3)-2) < tolerance);
	}
	
	@Test
	public void testPartialDiff() {
		
		//Create the term 2 x^2 y^3 / z		
		Monomial mon = new Monomial(1.0);
		mon.mult(x, 2).mult(y, 3).mult(z, -1).mult(2.0);
		
		//Differentiate the term 2 x^2 y^3 / z
		
		// wrt x => 4 x y^3 / z
		Monomial diffX = mon.partialDiff(x); 
		System.out.println(diffX);
		assertTrue(diffX.getCoefficient() == 4.0);
		assertTrue(diffX.getExponent(x) == 1.0);
		assertTrue(diffX.getExponent(y) == 3.0);
		assertTrue(diffX.getExponent(z) == -1.0);
		assertTrue(diffX.getExponent(w) == 0.0);

		
		// wrt y => 6 x^2 y^2 / z
		Monomial diffY = mon.partialDiff(y); 
		System.out.println(diffY);
		assertTrue(diffY.getCoefficient() == 6.0);
		assertTrue(diffY.getExponent(x) == 2.0);
		assertTrue(diffY.getExponent(y) == 2.0);
		assertTrue(diffY.getExponent(z) == -1.0);
		assertTrue(diffY.getExponent(w) == 0.0);
		
		// wrt z => - 2 x^2 y^3 / z^2
		Monomial diffZ = mon.partialDiff(z); 
		System.out.println(diffZ);
		assertTrue(diffZ.getCoefficient() == -2.0);
		assertTrue(diffZ.getExponent(x) == 2.0);
		assertTrue(diffZ.getExponent(y) == 3.0);
		assertTrue(diffZ.getExponent(z) == -2.0);
		assertTrue(diffZ.getExponent(w) == 0.0);
		
		// wrt w => 0
		Monomial diffW = mon.partialDiff(w); 
		System.out.println(diffW);
		assertTrue(diffW.getCoefficient() == 0.0);
		assertTrue(diffW.getExponent(x) == 0.0);
		assertTrue(diffW.getExponent(y) == 0.0);
		assertTrue(diffW.getExponent(z) == 0.0);
		assertTrue(diffW.getExponent(w) == 0.0);
		
	}
	
}


