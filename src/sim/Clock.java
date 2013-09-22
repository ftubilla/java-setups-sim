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
	private double metricsActualStartTime;
	private double metricsDesiredStartTime;
	private boolean isTimeToRecordData;
	
	public Clock(double metricsDesiredStartTime) {
		time = 0.0;
		this.metricsDesiredStartTime = metricsDesiredStartTime;
	}
	
	//Package-protected method. Only the sim object should be calling it.
	void advanceClockTo(double newTime){
		if (trace){
			logger.trace("Moving clock from " + time + " to " + newTime);
		}
		time = newTime;
		if (!isTimeToRecordData && time >= metricsDesiredStartTime){
			if (trace){logger.trace("Starting to record data at " + time);}
			metricsActualStartTime = time;
			isTimeToRecordData = true;
		}
	}
	
	public double getTime(){
		return time;
	}
	
	public boolean isTimeToRecordData(){
		return isTimeToRecordData;
	}
	
	/**
	 * Returns the first time at which the clock reached or exceeded the
	 * metrics recording start time.
	 * @return double
	 */
	public double getMetricsInitialTime(){
		return metricsActualStartTime;
	}
	
	/**
	 * Returns the total time during which metrics have been recorded.
	 * That is, the difference between current time and the metrics actual
	 * start time.
	 * @return double
	 */
	public double getMetricsRecordingTime(){
		return time - metricsActualStartTime;
	}
	
}


