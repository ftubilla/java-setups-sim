package sim;

import java.math.BigDecimal;

import lombok.EqualsAndHashCode;
import lombok.extern.apachecommons.CommonsLog;

/**
 * A class to represent a time instant or a period of time in the sim. This class has infinite precision,
 * which is key for maintaining precision in the sim regardless of the total simulation length.
 *  
 */
@EqualsAndHashCode(callSuper = false)
@CommonsLog
public class TimeInstant extends Number implements Comparable<TimeInstant> {

    public static final TimeInstant INFINITY = new TimeInstant(Double.MAX_VALUE, true);

    private static final long serialVersionUID = 1L;

    private final BigDecimal time;

    public TimeInstant(final double time) {
        this( new BigDecimal(time) );
    }

    public TimeInstant(final BigDecimal time){
        if ( time.compareTo(INFINITY.time) > 0 ) {
            log.debug(String.format("Truncating the time (%s) since it exceeds the max value", time));
            this.time = INFINITY.time;
        } else {
            this.time = time;
        }
    }

    private TimeInstant(final double time, boolean ignore) {
        this.time = new BigDecimal(time);
    }

    public TimeInstant add(final TimeInstant other) {
        return new TimeInstant(this.time.add(other.time));
    }

    public TimeInstant add(final double delta) {
        return new TimeInstant(this.time.add( new BigDecimal(delta)));
    }

    public TimeInstant subtract(final TimeInstant other) {
        return new TimeInstant(this.time.subtract(other.time));
    }

    public boolean hasReachedEpoch(TimeInstant timeInstant) {
        return this.compareTo(timeInstant) >= 0;
    }

    public boolean hasPassedEpoch(TimeInstant timeInstant) {
        return this.compareTo(timeInstant) > 0;
    }

    /**
     * Returns <tt>true</tt> if the current time instant has reach the given epoch (i.e., if the current
     * time is greater or equal to the given epoch time).
     * 
     * @param epoch
     * @return boolean
     */
    public boolean hasReachedEpoch(double epoch) {
        return this.hasReachedEpoch(new TimeInstant(epoch));
    }

    /**
     * Returns <tt>true</tt> if the clock has passed the given epoch (i.e., if the current
     * time is strictly greater than the given epoch).
     * @param epoch
     * @return
     */
    public boolean hasPassedEpoch(double epoch) {
        return this.hasPassedEpoch(new TimeInstant(epoch));
    }

    @Override
    public int intValue() {
        return this.time.intValue();
    }

    @Override
    public long longValue() {
        return this.time.longValue();
    }

    @Override
    public float floatValue() {
        return this.time.floatValue();
    }

    @Override
    public double doubleValue() {
        return this.time.doubleValue();
    }

    @Override
    public int compareTo(final TimeInstant other) {
        return this.time.compareTo(other.time);
    }

    @Override
    public String toString() {
        return String.format("%.5f", this.time.doubleValue());
    }

}
