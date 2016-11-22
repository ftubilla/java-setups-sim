package metrics.surplusstatistics;

import lombok.AccessLevel;
import lombok.Getter;

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
    private double minSurplus = Double.MAX_VALUE;
    private double initialTime;
    private double finalTime;

    @Getter(AccessLevel.NONE)
    private Double previousTime    = null;
    @Getter(AccessLevel.NONE)
    private Double previousSurplus = null;

    public void addPoint(double time, double surplus) {

        if (previousTime == null) {
            // First time the function is called
            initialTime = time;
        } else {

            if ( time < previousTime ) {
                throw new IllegalArgumentException(
                        String.format("A data point for time %.3f was given, but the calculator is currently at time %.3f",
                                time, this.previousTime));
            }
            
            if (time > initialTime) {
                double prevDeltaT = finalTime - initialTime;
                double newDeltaT = time - initialTime;
                averageInventory = (averageInventory * prevDeltaT
                        + findAreaAboveXAxis(previousTime, previousSurplus, time, surplus)) / newDeltaT;

                averageBacklog = (averageBacklog * prevDeltaT
                        + findAreaBelowXAxis(previousTime, previousSurplus, time, surplus)) / newDeltaT;

                serviceLevel = (serviceLevel * prevDeltaT
                        + findPeriodAboveXAxis(previousTime, previousSurplus, time, surplus)) / newDeltaT;

            }
        }

        if (surplus < minSurplus) {
            minSurplus = surplus;
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
