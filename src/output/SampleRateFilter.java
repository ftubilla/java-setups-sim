package output;

import sim.Sim;
import sim.TimeInstant;

public class SampleRateFilter implements IFilter {

    private TimeInstant samplePeriod;
    private TimeInstant lastRecordTime = new TimeInstant(-1);

    public SampleRateFilter(double samplePeriod) {
        this.samplePeriod = new TimeInstant(samplePeriod);
    }

    @Override
    public boolean passFilter(Sim sim) {
        boolean passFilter = false;
        TimeInstant currentTime = sim.getTime();

        if (currentTime.subtract(lastRecordTime).compareTo(samplePeriod) > 0) {
            passFilter = true;
            lastRecordTime = currentTime;
        }

        return passFilter;
    }

}
