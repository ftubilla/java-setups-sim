package optimization;

import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.Maps;

public class SingleIndexOptimizationVar<I1> implements Iterable<I1> {

	private final Map<I1, OptimizationVar> variables = Maps.newLinkedHashMap();
	private final String name;
	
	public SingleIndexOptimizationVar(String name, Iterable<I1> indexSet) {
		this.name = name;
		for (I1 i1 : indexSet){
			variables.put(i1, new OptimizationVar(name + "_" + i1));
		}
	}
	
	public OptimizationVar get(I1 index) {
		return variables.get(index);
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


