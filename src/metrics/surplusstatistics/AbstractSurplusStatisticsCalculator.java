package metrics.surplusstatistics;

public abstract class AbstractSurplusStatisticsCalculator {

    public abstract SurplusStatistics calculate();

    /**
     * Computes the area enclosed by the <em>positive</em> segment of the given
     * line and the x-axis.
     *
     */
    protected double findAreaAboveXAxis(double x1, double y1, double x2, double y2) {
        if (y1 <= 0 && y2 <= 0) {
            // Both points are below the x-axis
            return 0.0;
        } else {
            // Find the x-crossover point
            double xC = findYAxisIntersection(x1, y1, x2, y2);
            if (y1 <= 0 && y2 > 0) {
                // Slope is positive, set point 1 to crossover
                x1 = xC;
                y1 = 0.0;
            } else {
                if (y1 > 0 && y2 <= 0) {
                    // Slope is negative, set point 2 to crossover
                    x2 = xC;
                    y2 = 0.0;
                }
            }
        }
        assert y1 >= 0 && y2 >= 0 : "Check the area calculating function!";
        return Math.min(y1, y2) * (x2 - x1) + 0.5 * (x2 - x1) * Math.abs(y2 - y1);
    }

    protected double findAreaBelowXAxis(double x1, double y1, double x2, double y2) {
        return findAreaAboveXAxis(x1, -y1, x2, -y2);
    }

    protected double findPeriodAboveXAxis(double x1, double y1, double x2, double y2) {
        if (y1 <= 0 && y2 <= 0) {
            // Both points are below the x-axis
            return 0.0;
        }
        if (y1 > 0 && y2 > 0) {
            // Both points are above the x-axis
            return x2 - x1;
        }
        double xC = findYAxisIntersection(x1, y1, x2, y2);
        if (y1 <= 0 && y2 > 0) {
            // Slope is positive
            return x2 - xC;
        } else {
            // Slope is negative
            return xC - x1;
        }
    }

    protected double findYAxisIntersection(double x1, double y1, double x2, double y2) {
        return -y1 * (x2 - x1) / ((double) (y2 - y1)) + x1;
    }

}
