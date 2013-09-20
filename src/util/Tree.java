package util;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

public class Tree<T> {
	private static Logger logger = Logger.getLogger(Tree.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();

	@SuppressWarnings("unused")
	private boolean trace = logger.isTraceEnabled();
	
	private Node<T> root;
	
	public Tree(){
		root = new Node<T>(null);
	}
	
	/**
	 * Returns a set containing the nodes at the end of each branch of the tree.
	 * @return Set<Node<T>>
	 */
	public Set<Node<T>> getLeafNodes(){
		Set<Node<T>> leafNodes = new HashSet<Node<T>>();
		expandTree(root, leafNodes);
		return leafNodes;
	}
	
	private void expandTree(Node<T> fromNode, Set<Node<T>> leafNodes){
		if (fromNode.isLeaf()){
			leafNodes.add(fromNode);
			return;
		} else {
			for (Node<T> childNode : fromNode.getChildren()){
				expandTree(childNode, leafNodes);
			}
		}
	}
		
}


