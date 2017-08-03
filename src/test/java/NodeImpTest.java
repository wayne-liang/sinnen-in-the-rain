import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import implementations.ArcImpl;
import implementations.NodeImp;
import interfaces.Arc;
import interfaces.Node;

/**
 * JUnit class to test the Node implementation class
 * @author Darius
 *
 */
public class NodeImpTest {

	private static Node nodeA;
	private static Node nodeB;
	private static Node nodeC;
	private static Node nodeD;
	
	static Arc arcAB = new ArcImpl(1,nodeA, nodeB);
	static Arc arcAC = new ArcImpl(2,nodeA,nodeC);
	static Arc arcBD = new ArcImpl(2,nodeB,nodeD);
	static Arc arcCD = new ArcImpl(1,nodeC,nodeD);
	
	@BeforeClass
	public static void setupNodes(){
		nodeA = new NodeImp("A",2);
		nodeB = new NodeImp("B",3);
		nodeC = new NodeImp("C",3);
		nodeD = new NodeImp("D",2);
		
		arcAB = new ArcImpl(1,nodeA, nodeB);
		arcAC = new ArcImpl(2,nodeA,nodeC);
		arcBD = new ArcImpl(2,nodeB,nodeD);
		arcCD = new ArcImpl(1,nodeC,nodeD);
		
		nodeA.addOutArc(arcAB);
		nodeA.addOutArc(arcAC);
		nodeB.addInArc(arcAB);
		nodeC.addInArc(arcAC);
		
		nodeB.addOutArc(arcBD);
		nodeC.addOutArc(arcCD);
		nodeD.addInArc(arcBD);
		nodeD.addInArc(arcCD);
		
		
	}
	
	/**
	 * Tests for setting up node attributes.
	 */
	@Test
	public void testNodeSetup() {
		assertEquals(nodeA.getName(),"A");	
		assertEquals(nodeB.getWeight(),3);
	}
	
	/**
	 * Tests for setting up arc attributes.
	 */
	@Test
	public void testArcSetup(){
		assertEquals(2,arcAC.getWeight());
		assertEquals(arcCD.getSource(),nodeC);
		assertEquals(arcBD.getDestination(),nodeD);		
	}
	
	/**
	 * Tests that predecessor nodes are added successfully
	 */
	@Test
	public void testPredecessors(){
		assertTrue(nodeA.getPredecessors().isEmpty());
		
		List<Node> predD = new ArrayList<Node>();
		predD.add(nodeB);
		predD.add(nodeC);
		assertTrue(nodeD.getPredecessors().equals(predD));
	}
	
	/**
	 * Tests that successor nodes are added successfully
	 */
	@Test
	public void testSuccessors(){
		assertTrue(nodeD.getSuccessors().isEmpty());
		
		List<Node> succA = new ArrayList<Node>();
		succA.add(nodeB);
		succA.add(nodeC);
		assertTrue(nodeA.getSuccessors().equals(succA));
	}
	
	

}
