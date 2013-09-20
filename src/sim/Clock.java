package sim;

import org.apache.log4j.Logger;

/**
 * Holds the current time of the sim object. Useful for making it easy for
 * multiple sim objects to coexist together.
 * 
 * @author ftubilla
 *
 */
public class Clock {
	private static Logger logger = Logger.getLogger(Clock.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();
	private boolean trace = logger.isTraceEnabled();
	
	private double time;
	
	public Clock() {
		time = 0.0;
	}
	
	//Package-protected method. Only the sim object should be calling it.
	void advanceClockTo(double newTime){
		if (trace){
			logger.trace("Moving clock from " + time + " to " + newTime);
		}
		time = newTime;
	}
	
	public double getTime(){
		return time;
	}
	
	
}


