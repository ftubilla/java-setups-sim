package processes.generators;

import java.util.Random;

import junit.framework.TestCase;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Test;

import output.Recorder;

public class RandomGeneratorsTest extends TestCase {

	@Before
	public void setUp(){
		PropertyConfigurator.configure("config/log4j.properties");
	}
	
	@Test
	public void testOverlappingSumsDiehard() {
		Recorder recorder = new Recorder("output/test.txt");
		Random generatorJava = new Random();
		MersenneTwisterFast generatorMT = new MersenneTwisterFast();
		
		int samples = 500;
		int sumsInSample = 100;

		
		recorder.record(new Object[] {"SAMPLE","GENERATOR","VALUE"});
		
		Object[] row = new Object[3];
		
		row[1] = "JAVA";
		double sum;
		for (int i=0; i<samples; i++){
			row[0] = i;
			sum = 0.0;
			for (int j=0; j<sumsInSample; j++){
				sum += generatorJava.nextDouble();
			}
			row[2] = sum;
			recorder.record(row);
		}
		
		row[1] = "MT";
		for (int i=0; i<samples; i++){
			row[0] = i;
			sum = 0.0;
			for (int j=0; j<sumsInSample; j++){
				sum += generatorMT.nextDouble();
			}
			row[2] = sum;
			recorder.record(row);
		}
				
		recorder.close();
	}
}


