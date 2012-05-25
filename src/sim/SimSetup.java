/*
 * Written by Fernando Tubilla
 * ftubilla@mit.edu
 * © 2012 Fernando Tubilla. All rights reserved.
 */

package sim;

import java.io.File;
import org.codehaus.jackson.map.ObjectMapper;

public class SimSetup {

	public static void setup(Sim sim){
		
		try{
			ObjectMapper mapper = new ObjectMapper();
			Params params = mapper.readValue(new File("json/inputs.json"), Params.class);
			sim.setParams(params);
		} 
		catch(Exception e){
			System.err.println("Problem reading input json file!");
		}		
	}
}
