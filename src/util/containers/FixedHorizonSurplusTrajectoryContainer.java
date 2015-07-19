package util.containers;

import java.util.Collection;
import java.util.NavigableSet;
import java.util.TreeSet;

import sim.Sim;

import lombok.extern.apachecommons.CommonsLog;

/**
 * A surplus trajectory container of fixed time horizon (i.e., the trajectory 
 * encompasses a time range no greater than a desired fixed value).
 * 
 * @author ftubilla
 *
 */
@CommonsLog
public class FixedHorizonSurplusTrajectoryContainer implements ISurplusTrajectoryContainer {

	private double[] surplusTargets;
	private int numItems;
	private double timeHorizon;
	private NavigableSet<SurplusDataPoint> dataPoints;
	
	public FixedHorizonSurplusTrajectoryContainer(double timeHorizon, Collection<Double> surplusTargets){
		this(timeHorizon, arrayFromCollection(surplusTargets));
	}
	
	public FixedHorizonSurplusTrajectoryContainer(double timeHorizon, double[] surplusTargets){
		this.timeHorizon = timeHorizon;
		this.surplusTargets = surplusTargets;
		this.numItems = surplusTargets.length;
		this.dataPoints = new TreeSet<SurplusDataPoint>();
	}
		
	/**
	 * Adds the given data point. If the time is decreasing or if the surplus is above
	 * the given target for any of the items, an @code{Error} is raised. If
	 * the given time will cause the container to exceed the time horizon, a sufficient amount 
	 * of earlier time points will be removed and an initial point will be inserted at the
	 * interpolated time.
	 * 
	 */
	@Override
	public void addPoint(double time, double[] surplus) {
		assert surplus.length == numItems : "Inconsistent number of items";
		boolean isBelowTarget = true;
		for (int i=0; i<surplus.length; i++){
			if (surplus[i] > surplusTargets[i] + Sim.SURPLUS_TOLERANCE) {
				isBelowTarget = false;
			}
		}
		boolean isNonDecreasing = true;
		if (!dataPoints.isEmpty() && time < dataPoints.last().getTime()) {
			isNonDecreasing = false;
		}
		if (!isBelowTarget || !isNonDecreasing){
			throw new Error("DataPoint is invalid. Below Target?" + isBelowTarget + " NonDecreasing?" + isNonDecreasing);
		}
		Double[] startSurplus = getInterpolatedSurplus(time - timeHorizon);
		while (!dataPoints.isEmpty() && (time - dataPoints.first().getTime()) > timeHorizon) {
			//Remove the head of the list until the horizon has shrunk enough
			dataPoints.pollFirst();
		}
		if (startSurplus != null){
			dataPoints.add(new SurplusDataPoint(time-timeHorizon, startSurplus));
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
	
	public ISurplusTrajectoryContainer copy() {
		FixedHorizonSurplusTrajectoryContainer copy = new FixedHorizonSurplusTrajectoryContainer(timeHorizon, surplusTargets);
		for (SurplusDataPoint dp : dataPoints){
			copy.addPoint(dp.getTime(), dp.getSurplus());
		}
		return copy;
	}

	public static double[] arrayFromCollection(Collection<Double> coll){
		double[] array = new double[coll.size()];
		int i=0;
		for (double item : coll){
			array[i++] = item;
		}
		return array;
	}
	
}


