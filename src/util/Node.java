package util;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * A node of a tree. Nodes can hold objects of type T, but their equality test is based on 
 * object instance. Thus, two different instances with the same content correspond to two
 * different nodes!
 * 
 * @author ftubilla
 *
 * @param <T>
 */
public class Node<T> {
	
	private static int nodesCreated=0;
	private static Logger logger = Logger.getLogger(Node.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();
	private boolean trace = logger.isTraceEnabled();

	private int id;
	private T content;
	private Node<T> parent;
	private Set<Node<T>> children;	
		
	public Node(T content){
		this.id = nodesCreated++;
		this.content = content;
		children = new HashSet<Node<T>>();
		if (trace){
			logger.trace("Creating new " + this);
		}
	}
			
	public void addChild(Node<T> child){
		assert child.getParent() == null : "A node can only have one parent in a tree!";
		children.add(child);
		child.setParent(this);
	}
	
	public Node<T> getParent(){
		return parent;
	}
	
	protected void setParent(Node<T> parent){
		this.parent = parent;
	}
	
	public Iterable<Node<T>> getChildren(){
		return children;
	}
	
	public boolean isLeaf(){
		return children.size()==0;
	}
	
	public T getContent(){
		return content;
	}
	
	public boolean isRoot(){
		return parent==null;
	}
	
	public int getId(){
		return id;
	}
	
	@Override
	public String toString(){		
		return "Node:"+getId()+"(Content:"+content+")";
	}

	@Override
	public int hashCode() {
		if (content == null)
			return 0;
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Node))
			return false;
		Node<?> other = (Node<?>) obj;
		if (id!=other.getId())
			return false;
		return true;
	}
	
	
}


