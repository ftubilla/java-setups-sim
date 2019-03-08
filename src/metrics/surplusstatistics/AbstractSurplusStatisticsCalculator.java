package metrics.surplusstatistics;

import sim.TimeInstant;

public abstract class AbstractSurplusStatisticsCalculator {

    public abstract SurplusStatistics calculate();

    /**
     * Computes the area enclosed by the <em>positive</em> segment of the given
     * line and the time-axis (i.e., x axis).
     *
     */
    protected double findAreaAboveTimeAxis(TimeInstant t1, double y1, TimeInstant t2, double y2) {
        if (y1 <= 0 && y2 <= 0) {
            // Both points are below the x-axis
            return 0.0;
        } else {
            // Find the x-crossover point
            if (y1 <= 0 && y2 > 0) {
                // Slope is positive, set point 1 to crossover
                t1 = findCrossoverTime(t1, y1, t2, y2);
                y1 = 0.0;
            } else {
                if (y1 > 0 && y2 <= 0) {
                    // Slope is negative, set point 2 to crossover
                    t2 = findCrossoverTime(t1, y1, t2, y2);
                    y2 = 0.0;
                }
            }
        }
        assert y1 >= 0 && y2 >= 0 : "Check the area calculating function!";
        double timeDelta = t2.subtract(t1).doubleValue();
        return Math.min( y1, y2 ) * timeDelta + 0.5 * timeDelta * Math.abs( y2 - y1 );
    }

    protected double findAreaBelowTimeAxis(TimeInstant t1, double y1, TimeInstant t2, double y2) {
        return findAreaAboveTimeAxis(t1, -y1, t2, -y2);
    }

    protected double findPeriodAboveTimeAxis(TimeInstant t1, double y1, TimeInstant t2, double y2) {
        if (y1 <= 0 && y2 <= 0) {
            // Both points are below the x-axis
            return 0.0;
        }
        if (y1 > 0 && y2 > 0) {
            // Both points are above the x-axis
            return t2.subtract(t1).doubleValue();
        }
        TimeInstant tC = findCrossoverTime(t1, y1, t2, y2);
        if (y1 <= 0 && y2 > 0) {
            // Slope is positive
            return t2.subtract(tC).doubleValue();
        } else {
            // Slope is negative
            return tC.subtract(t1).doubleValue();
        }
    }

    /**
     * Returns the time instant at which a trajectory crosses the 0-surplus line.
     * 
     * @param t1
     * @param y1
     * @param t2
     * @param y2
     * @return timeInstant
     */
    protected TimeInstant findCrossoverTime(TimeInstant t1, double y1, TimeInstant t2, double y2) {
        return t1.add( -y1 * ( t2.subtract(t1).doubleValue() ) / ( (double) (y2 - y1) ) );
    }

}
