package sequences;

import java.util.Comparator;

import lombok.Data;
import system.Item;

/**
 * Compares optimal f-cyclic schedules by cost, breaking ties (up to the given tolerance) by
 * sequence size and lexicographically by item id.
 * 
 * @author ftubilla
 *
 */
@Data
public class OptimalFCyclicScheduleComparator implements Comparator<OptimalFCyclicSchedule> {

    private final double costTolerance;

    @Override
    public int compare(OptimalFCyclicSchedule schedule1, OptimalFCyclicSchedule schedule2) {
        double schedule1Cost = schedule1.getScheduleCost();
        double schedule2Cost = schedule2.getScheduleCost();
        if ( Math.abs(schedule1Cost - schedule2Cost) <= this.costTolerance * Math.max(schedule1Cost, schedule2Cost) ) {
            // Same cost; break tie by size
            int sizeCompare = Integer.compare(schedule1.getSequence().getSize(), schedule2.getSequence().getSize());
            if ( sizeCompare == 0 ) {
                // Break tie lexicographically
                for ( int i = 0; i < schedule1.getSequence().getSize(); i++ ) {
                    Item item1 = schedule1.getSequence().getItemAtPosition(i);
                    Item item2 = schedule2.getSequence().getItemAtPosition(i);
                    if ( item1.equals(item2) ) {
                        continue;
                    } else {
                        return Integer.compare(item1.getId(), item2.getId());
                    }
                }
            } else {
                return sizeCompare;
            }
        } else {
            return Double.compare(schedule1Cost, schedule2Cost);
        }
        return 0;
    }

}
