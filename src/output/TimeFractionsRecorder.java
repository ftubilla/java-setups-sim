package output;

import metrics.TimeFractionsMetrics;
import sim.Sim;
import system.Item;

public class TimeFractionsRecorder extends Recorder {

	public enum Column {SIM_ID, METRIC, ITEM, VALUE};

	public TimeFractionsRecorder(){
		super("output/time_fractions.txt");
		super.writeHeader(Column.class);
	}

	@Override
	public void recordEndOfSim(Sim sim){

		TimeFractionsMetrics fractions = sim.getMetrics().getTimeFractions();

		for (TimeFractionsMetrics.Metric metric : TimeFractionsMetrics.Metric.values()) {
			for (Item item : sim.getMachine()) {
				Object[] row = new Object[4];
				row[0] = sim.getId();
				row[1] = metric;
				row[2] = item.getId();
				row[3] = fractions.getFraction(metric, item);
				record(row);
			}
		}

	}
	
}


