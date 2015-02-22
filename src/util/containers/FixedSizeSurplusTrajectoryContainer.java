package util.containers;

import java.util.NavigableSet;
import java.util.TreeSet;

import lombok.extern.apachecommons.CommonsLog;

/**
 * A surplus trajectory container of fixed size.
 * @author ftubilla
 *
 */
@CommonsLog
public class FixedSizeSurplusTrajectoryContainer implements ISurplusTrajectoryContainer {

	private double[] surplusTargets;
	private int numItems;
	private int numDataPoints;
	private NavigableSet<SurplusDataPoint> dataPoints;
	
	public FixedSizeSurplusTrajectoryContainer(int numDataPoints, double[] surplusTargets){
		this.numDataPoints = numDataPoints;
		this.surplusTargets = surplusTargets;
		this.numItems = surplusTargets.length;
		this.dataPoints = new TreeSet<SurplusDataPoint>();
	}
		
	/**
	 * Adds the given data point; if the number of points exceeds the total data points,
	 * the earliest data point (based on the time) is removed.
	 */
	@Override
	public void addPoint(double time, double[] surplus) {
		assert surplus.length == numItems : "Inconsistent number of items";		
		if (dataPoints.size() == numDataPoints){
			//Remove the head of the list
			dataPoints.pollFirst();
		}
		dataPoints.add(new SurplusDataPoint(time, surplus));
		}

	@Override
	public Double[] getInterpolatedSurplus(double time) {
		SurplusDataPoint floorDP = dataPoints.floor(new SurplusDataPoint(time));
		SurplusDataPoint ceilDP = dataPoints.ceiling(new SurplusDataPoint(time));
		if (floorDP == null || ceilDP == null){
			log.debug(String.format("Container does not have an entry for %.2f because it's outside of the " +
					"current range [%.2f,%.2f]", time, this.getEarliestTime(), this.getLatestTime()));
			return null;
		}
		Double[] interpolatedSurplus = new Double[numItems];
		double lambda = (time - floorDP.getTime()) / (ceilDP.getTime() - floorDP.getTime());
		for (int i=0; i<numItems; i++){
			double surplusFloor = floorDP.getSurplus()[i];
			double surplusCeil = ceilDP.getSurplus()[i];
			double surplus = surplusFloor * (1 - lambda) + surplusCeil * lambda;
			interpolatedSurplus[i] = surplus;
		}
		return interpolatedSurplus;
	}

	@Override
	public double[] getSurplusDeviationArea() {
		double[] devArea = new double[numItems];
		for (int i=0; i<numItems; i++){
			double prevDev = 0.0;
			double prevTime = 0.0;
			boolean isFirst = true;
			for (SurplusDataPoint dp : dataPoints){
				double dev = surplusTargets[i] - dp.getSurplus()[i];
				double time = dp.getTime();
				if (!isFirst){
					double aveHeight = 0.5 * (prevDev + dev);
					double base = time - prevTime;
					double area = base * aveHeight;
					devArea[i] += area;
				} 
				isFirst = false;
				prevDev = dev;
				prevTime = time;
			}			
		}
		return devArea;
	}

	@Override
	public Double getEarliestTime() {
		if (dataPoints.isEmpty()){
			return null;
		}
		return dataPoints.first().getTime();
	}
	
	@Override
	public Double getLatestTime() {
		if (dataPoints.isEmpty()) {
			return null;
		}
		return dataPoints.last().getTime();
	}

}


