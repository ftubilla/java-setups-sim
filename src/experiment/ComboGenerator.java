package experiment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import util.Node;
import util.Tree;


/**
 * Generates combinations from the cross products of elements within the given sets.
 * 
 * @author ftubilla
 *
 * @param <T>
 */
public class ComboGenerator<T> {
	
	private static Logger logger = Logger.getLogger(ComboGenerator.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();

	@SuppressWarnings("unused")
	private boolean trace = logger.isTraceEnabled();

	private List<Iterable<T>> iterables;
	private int numSets=0;
	
	public ComboGenerator(){
		iterables = new ArrayList<Iterable<T>>();
	}
	
	/**
	 * Adds a new set of elements for generating the cross products. The elements within
	 * each combo have the same order as the ordering used for adding sets. For example,
	 * if S1={1,2,3} and S2={A,B}, then a combo would be (1,A).
	 * 
	 * @param iterable
	 */
	public void addSet(Iterable<T> iterable){
		iterables.add(iterable);
		numSets++;
	}

	public Iterable<Combo<T>> generateCombos(){
		
		Tree<T> comboTree = new Tree<T>();
		
		for (Iterable<T> iterable : iterables){			
			Set<Node<T>> leafNodes = comboTree.getLeafNodes();
			for (T element : iterable){
				for (Node<T> leafNode : leafNodes){
					Node<T> newNode = new Node<T>(element);
					leafNode.addChild(newNode);
				}
			}			
		}
		
		List<Combo<T>> combos = new ArrayList<Combo<T>>();
		for (Node<T> leafNode : comboTree.getLeafNodes()){
			
			Combo<T> combo = new Combo<T>(numSets);
			Node<T> currentNode = leafNode;
			while(!currentNode.isRoot()){
				combo.add(currentNode.getContent());
				currentNode = currentNode.getParent();
			}
			combos.add(combo);
		}
		
		return combos;
	}


}


