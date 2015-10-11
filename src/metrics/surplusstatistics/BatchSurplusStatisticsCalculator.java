package metrics.surplusstatistics;

import java.util.Collection;

import org.apache.commons.lang3.tuple.Pair;

/**
 * Computes the surplus statistics by getting a whole batch of data points at once,
 * instead of one at a time.
 * 
 * @author ftubilla
 *
 */
public class BatchSurplusStatisticsCalculator extends AbstractSurplusStatisticsCalculator {

	private final StreamSurplusStatisticsCalculator streamCalculator = new StreamSurplusStatisticsCalculator();
	
	public void addPoints(Collection<Pair<Double, Double>> dataPoints) {
		translateAndAdd(0.0, dataPoints);
	}
	
	public void translateAndAdd(double offset, Collection<Pair<Double, Double>> dataPoints) {
		dataPoints.stream().forEach( pair -> streamCalculator.addPoint(pair.getLeft(), pair.getRight() + offset));
	}
	
	@Override
	public SurplusStatistics calculate() {
		return streamCalculator.calculate();
	}

	/**
	 * A convenience static method for calculating surplus statistics after applying some offset to the data points.
	 * @param offset
	 * @param dataPoints
	 * @return surplusStatistics
	 */
	public static SurplusStatistics translateAndCalculate(double offset, Collection<Pair<Double, Double>> dataPoints) {
		BatchSurplusStatisticsCalculator calculator = new BatchSurplusStatisticsCalculator();
		calculator.translateAndAdd(offset, dataPoints);
		return calculator.calculate();
	}
	
	/**
	 * A convenience static method for calculating surplus statistics for a set of data points.
	 * 
	 * @param dataPoints
	 * @return surplusStatistics
	 */
	public static SurplusStatistics calculate(Collection<Pair<Double, Double>> dataPoints) {
		BatchSurplusStatisticsCalculator calculator = new BatchSurplusStatisticsCalculator();
		calculator.addPoints(dataPoints);
		return calculator.calculate();
	}
	
}
