package optimization;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.joptimizer.functions.ConvexMultivariateRealFunction;

public class OptimizationProblemTest {

	/**
	 * Tests that the encapsulating library works by solving a 2D
	 * quadratic optimization program.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test2DQuadratic() throws Exception {
		
		OptimizationProblem prob = new OptimizationProblem("test_problem");
		OptimizationVar x0 = new OptimizationVar("x0");
		OptimizationVar x1 = new OptimizationVar("x1");
		OptimizationConstraint c1 = new OptimizationConstraint("nonnegative_x0");
		OptimizationConstraint c2 = new OptimizationConstraint("nonnegative_x1");
		OptimizationConstraint c3 = new OptimizationConstraint("sum_eq_1");
		
		c1.addTerm(x0, 1.0).gEql(0.0);
		c2.addTerm(x1, 1.0).gEql(0.0);
		c3.addTerm(x0, 1.0).addTerm(x1, 1.0).eql(1.0);
		x0.setInitialValue(0);
		x1.setInitialValue(1);
		
		ConvexMultivariateRealFunction obj = new ConvexMultivariateRealFunction() {
			
			@Override
			public double value(double[] X) {
				return X[0]*X[0] + X[1]*X[1];
			}
			
			@Override
			public double[][] hessian(double[] X) {
				double[][] hess = new double[2][2];
				hess[0][0] = 2;
				hess[0][1] = 0;
				hess[1][0] = 0;
				hess[1][1] = 2;
				return hess;
			}
			
			@Override
			public double[] gradient(double[] X) {
				double[] grad = new double[2];
				grad[0] = 2*X[0];
				grad[1] = 2*X[1];
				return grad;
			}
			
			@Override
			public int getDim() {
				return 2;
			}
		};
		
		prob.addVar(x0);
		prob.addVar(x1);
		prob.addConstraint(c1);
		prob.addConstraint(c2);
		prob.addConstraint(c3);
		prob.setObj(obj);
		System.out.println(prob);
		System.out.println(prob.solve());
		assertTrue(Math.abs(x0.getSol() - 0.5) < 1e-5);
		assertTrue(Math.abs(x1.getSol() - 0.5) < 1e-5);
		
	}

	/**
	 * Tests a random problem whose solution has been verified with Excel. This problem
	 * is not convex actually...
	 * @throws Exception
	 */
	@Test
	public void test2DExample() throws Exception {
		
		OptimizationProblem prob = new OptimizationProblem("test2DExample");
		OptimizationVar x = new OptimizationVar("x");
		OptimizationVar y = new OptimizationVar("y");
		OptimizationConstraint c1 = new OptimizationConstraint("c1");
		c1.addTerm(x, 1).addTerm(y, 1).gEql(2);
		OptimizationConstraint c2 = new OptimizationConstraint("c2");
		c2.addTerm(x, 1).lEql(1);
		OptimizationConstraint c3 = new OptimizationConstraint("c3");
		c3.addTerm(y, 1).lEql(2);
		
		ConvexMultivariateRealFunction obj = new ConvexMultivariateRealFunction() {
			
			@Override
			public double value(double[] X) {
				return 10*Math.pow(X[0], 3) - 4*X[0]*X[1];
			}
			
			@Override
			public double[][] hessian(double[] X) {
				double[][] hess = new double[2][2];
				hess[0][0] = 60*X[0];
				hess[0][1] = -4;
				hess[1][0] = -4;
				hess[1][1] = 0;
				return hess;
			}
			
			@Override
			public double[] gradient(double[] X) {
				double[] grad = new double[2];
				grad[0] = 30*Math.pow(X[0], 2) - 4*X[1];
				grad[1] = -4*X[0];
				return(grad);
			}
			
			@Override
			public int getDim() {
				return 2;
			}
		};
		
		prob.addVar(x);
		prob.addVar(y);
		prob.addConstraint(c1);
		prob.addConstraint(c2);
		prob.addConstraint(c3);
		prob.setObj(obj);
		
		System.out.println(prob + "");
		prob.solve();
		System.out.println("x = " + x.getSol());
		System.out.println("y = " + y.getSol());
		assertTrue(x.getSol() + "!= 0.516397779512217", Math.abs(x.getSol() - 0.516397779512217) < 1e-5);
		assertTrue(y.getSol() + "!= 2.0", Math.abs(y.getSol() - 2.0) < 1e-5);
		
	}
	
}


