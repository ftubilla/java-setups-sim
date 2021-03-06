package metrics.surplusstatistics;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import sim.TimeInstant;

/**
 * Computes the surplus statistics by getting a whole batch of data points at
 * once, instead of one at a time.
 * 
 * @author ftubilla
 *
 */
public class BatchSurplusStatisticsCalculator extends AbstractSurplusStatisticsCalculator {

    private final StreamSurplusStatisticsCalculator streamCalculator = new StreamSurplusStatisticsCalculator();

    public void addPoints(final List<Pair<TimeInstant, Double>> dataPoints) {
        translateAndAdd(0.0, dataPoints);
    }

    public void translateAndAdd(final double offset, final List<Pair<TimeInstant, Double>> dataPoints) {
        dataPoints.stream().forEach(pair -> streamCalculator.addPoint(pair.getLeft(), pair.getRight() + offset));
    }

    @Override
    public SurplusStatistics calculate() {
        return streamCalculator.calculate();
    }

    /**
     * A convenience static method for calculating surplus statistics after
     * applying some offset to the data points.
     * 
     * @param offset
     * @param dataPoints
     * @return surplusStatistics
     */
    public static SurplusStatistics translateAndCalculate(final double offset, final List<Pair<TimeInstant, Double>> dataPoints) {
        BatchSurplusStatisticsCalculator calculator = new BatchSurplusStatisticsCalculator();
        calculator.translateAndAdd(offset, dataPoints);
        return calculator.calculate();
    }

    /**
     * A convenience static method for calculating surplus statistics for a set
     * of data points.
     * 
     * @param dataPoints
     * @return surplusStatistics
     */
    public static SurplusStatistics calculate(final List<Pair<TimeInstant, Double>> dataPoints) {
        BatchSurplusStatisticsCalculator calculator = new BatchSurplusStatisticsCalculator();
        calculator.addPoints(dataPoints);
        return calculator.calculate();
    }

}
