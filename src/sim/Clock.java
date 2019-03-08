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

    private TimeInstant time;
    private double  metricsActualStartTime;
    private double  metricsDesiredStartTime;
    private boolean isTimeToRecordData;

    public Clock(double metricsDesiredStartTime) {
        this.time = new TimeInstant(0); 
        this.metricsDesiredStartTime = metricsDesiredStartTime;
    }

    void advanceClockBy(double timeDelta) {
        this.advanceClockTo( this.time.add(timeDelta) );
    }

    // Package-protected method. Only the sim object should be calling it.
    void advanceClockTo(TimeInstant newTime) {
        if (trace) {
            logger.trace("Moving clock from " + time + " to " + newTime);
        }
        time = newTime;
        if (!isTimeToRecordData && time.doubleValue() >= metricsDesiredStartTime) {
            if (trace) {
                logger.trace("Starting to record data at " + time);
            }
            metricsActualStartTime = time.doubleValue();
            isTimeToRecordData = true;
        }
    }

    public TimeInstant getTime() {
        return time;
    }

    public boolean isTimeToRecordData() {
        return isTimeToRecordData;
    }

    /**
     * Returns the first time at which the clock reached or exceeded the metrics
     * recording start time.
     * 
     * @return double
     */
    public double getMetricsInitialTime() {
        return metricsActualStartTime;
    }

    public boolean hasReachedEpoch(TimeInstant timeInstant) {
        return this.time.hasReachedEpoch(timeInstant);
    }

    public boolean hasPassedEpoch(TimeInstant timeInstant) {
        return this.time.hasPassedEpoch(timeInstant);
    }

    /**
     * Returns <tt>true</tt> if the clock has reach the given epoch (i.e., if the current
     * time is greater or equal to the given epoch time).
     * 
     * @param epoch
     * @return boolean
     */
    public boolean hasReachedEpoch(double epoch) {
        return this.time.hasReachedEpoch(epoch);
    }

    /**
     * Returns <tt>true</tt> if the clock has passed the given epoch (i.e., if the current
     * time is strictly greater than the given epoch).
     * @param epoch
     * @return
     */
    public boolean hasPassedEpoch(double epoch) {
        return this.time.hasPassedEpoch(epoch);
    }

    /**
     * Returns the total time during which metrics have been recorded. That is,
     * the difference between current time and the metrics actual start time.
     * 
     * @return double
     */
    public double getMetricsRecordingTime() {
        return this.time.doubleValue() - metricsActualStartTime;
    }

    @Override
    public String toString() {
        return this.time.toString();
    }

}
