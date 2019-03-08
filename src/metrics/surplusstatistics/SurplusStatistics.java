package metrics.surplusstatistics;

import sim.TimeInstant;

public interface SurplusStatistics {

    public TimeInstant getInitialTime();

    public TimeInstant getFinalTime();

    public double getAverageInventory();

    public double getAverageBacklog();

    public double getServiceLevel();

    public double getMinSurplus();

    public double getMaxSurplus();
}
