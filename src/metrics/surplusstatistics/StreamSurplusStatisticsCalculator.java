package metrics.surplusstatistics;

import lombok.AccessLevel;
import lombok.Getter;
import sim.TimeInstant;

/**
 * Computes the surplus statistics on-line, without the need to store the whole
 * trajectory.
 * 
 * @author ftubilla
 *
 */
@Getter
public class StreamSurplusStatisticsCalculator extends AbstractSurplusStatisticsCalculator
        implements SurplusStatistics {

    private double averageInventory;
    private double averageBacklog;
    private double serviceLevel;
    private double minSurplus = Double.POSITIVE_INFINITY;
    private double maxSurplus = Double.NEGATIVE_INFINITY;
    private TimeInstant initialTime;
    private TimeInstant finalTime;

    @Getter(AccessLevel.NONE)
    private TimeInstant previousTime    = null;
    @Getter(AccessLevel.NONE)
    private Double previousSurplus = null;

    public void addPoint(TimeInstant time, double surplus) {

        if (previousTime == null) {
            // First time the function is called
            initialTime = time;
        } else {

            if ( !time.hasReachedEpoch(previousTime) ) {
                throw new IllegalArgumentException(
                        String.format("A data point for time %s was given, but the calculator is currently at time %s",
                                time, this.previousTime));
            }
            
            if (time.hasPassedEpoch(initialTime)) {
                double prevDeltaT = finalTime.subtract(initialTime).doubleValue();
                double newDeltaT = time.subtract(initialTime).doubleValue();
                averageInventory = (averageInventory * prevDeltaT
                        + findAreaAboveTimeAxis(previousTime, previousSurplus, time, surplus)) / newDeltaT;

                averageBacklog = (averageBacklog * prevDeltaT
                        + findAreaBelowTimeAxis(previousTime, previousSurplus, time, surplus)) / newDeltaT;

                serviceLevel = (serviceLevel * prevDeltaT
                        + findPeriodAboveTimeAxis(previousTime, previousSurplus, time, surplus)) / newDeltaT;

            }
        }

        if (surplus < minSurplus) {
            minSurplus = surplus;
        }
        if ( surplus > maxSurplus ) {
            maxSurplus = surplus;
        }
        previousTime = time;
        previousSurplus = surplus;
        finalTime = time;
    }

    @Override
    public SurplusStatistics calculate() {
        return this;
    }

}
