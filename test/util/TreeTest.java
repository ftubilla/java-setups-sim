package util;

import org.apache.log4j.Logger;
import org.junit.Test;

public class TreeTest {
	private static Logger logger = Logger.getLogger(TreeTest.class);

	@SuppressWarnings("unused")
	private boolean debug = logger.isDebugEnabled();

	@SuppressWarnings("unused")
	private boolean trace = logger.isTraceEnabled();

	@Test
	public void test() {
		
		Tree<Integer> tree = new Tree<Integer>();
		int counter=0;
		for (int i=1; i<=3; i++){
			System.out.println("New level");
			for (Node<Integer> leaf : tree.getLeafNodes()){				
				Node<Integer> nodeL = new Node<Integer>(counter++);
				Node<Integer> nodeR = new Node<Integer>(counter++);
				leaf.addChild(nodeL);
				leaf.addChild(nodeR);
			}
		}
		
		for (Node<Integer> leafNode : tree.getLeafNodes()){
			System.out.println("Leaf " + leafNode);
		}
				
	}
}


