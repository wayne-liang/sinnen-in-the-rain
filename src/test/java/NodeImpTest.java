import implementations.structures.ArcImpl;
import implementations.structures.NodeImp;
import interfaces.structures.Arc;
import interfaces.structures.Node;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

		assertTrue(nodeD.getPredecessors().contains(nodeB));
		assertTrue(nodeD.getPredecessors().contains(nodeC));
	}
	
	/**
	 * Tests that successor nodes are added successfully
	 */
	@Test
	public void testSuccessors(){
		assertTrue(nodeD.getSuccessors().isEmpty());

		assertTrue(nodeA.getSuccessors().contains(nodeB));
		assertTrue(nodeA.getSuccessors().contains(nodeC));
	}
	
	

}
