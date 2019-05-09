package optimization;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class DoubleIndexOptimizationVar<I1, I2> implements Iterable<I1> {

    private final Map<I1, SingleIndexOptimizationVar<I2>> variables = Maps.newLinkedHashMap();
    private final Set<OptimizationVar> optimizationVars;
    private final String                                  name;

    public DoubleIndexOptimizationVar(String name, Iterable<I1> indexSet1, Iterable<I2> indexSet2) {
        this.name = name;
        this.optimizationVars = Sets.newHashSet();
        for (I1 i1 : indexSet1) {
            SingleIndexOptimizationVar<I2> rowVars = new SingleIndexOptimizationVar<I2>(name + "_" + i1, indexSet2); 
            variables.put(i1, rowVars);
            rowVars.getOptimizationVars().forEach( x -> this.optimizationVars.add(x) );
        }
    }

    public OptimizationVar get(I1 index1, I2 index2) {
        SingleIndexOptimizationVar<I2> varRow = variables.get(index1);
        if (varRow != null) {
            return varRow.get(index2);
        } else {
            return null;
        }
    }

    public Iterable<I2> get(I1 index1) {
        if (variables.containsKey(index1)) {
            return variables.get(index1);
        } else {
            return Lists.<I2> newArrayList();
        }
    }

    public Iterable<OptimizationVar> getOptimizationVars() {
        return Collections.unmodifiableSet(this.optimizationVars);
    }

    @Override
    public Iterator<I1> iterator() {
        return variables.keySet().iterator();
    }

    @Override
    public String toString() {
        return name;
    }

}
