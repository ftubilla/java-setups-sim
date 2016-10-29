package util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class UtilMethods {

    /**
     * A shorthand for creating an immutable list of doubles, using the same
     * notation as in R. This function is intended to simplify the verbosity
     * of unit tests.
     * 
     * @param elements
     * @return immutable list
     */
    public static ImmutableList<Double> c(double... elements) {
        Builder<Double> builder = ImmutableList.builder();
        for ( double element : elements ) {
            builder.add(element);
        }
        return builder.build();
    }

}
