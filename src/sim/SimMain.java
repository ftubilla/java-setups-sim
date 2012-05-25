/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * © 2012 Fernando Tubilla. All rights reserved.
 */

package sim;

public class SimMain {

	static Sim sim;
	
	public static void main(String[] args){
		
		sim = new Sim();
		SimSetup.setup(sim);
		SimRun.run(sim);
		
	}
	
	
}
