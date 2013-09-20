package experiment;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


/**
 * Represents an element of the set formed by taking the cross-product of a fixed number of sets.
 * @author ftubilla
 *
 * @param <T>
 */
public class Combo<T> {
	private static Logger logger = Logger.getLogger(Combo.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();

	@SuppressWarnings("unused")
	private boolean trace = logger.isTraceEnabled();
	
	private int numSets;
	private List<T> elements;
	
	public Combo(int numSets){
		this.numSets = numSets;
		elements = new ArrayList<T>(numSets);
	}
	
	public void add(T element){
		elements.add(element);
	}
	
	public void set(int index, T element){
		elements.set(index, element);
	}
	
	public T get(int index){
		assert index < numSets : "The combo goes only to index " + (numSets-1);
		return elements.get(index);
	}
		
	public Iterable<T> getValues(){
		return elements;
	}
	
}


