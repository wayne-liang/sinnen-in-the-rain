package algorithm;

import implementations.ConversionImp;
import implementations.SchedulerTime;
import implementations.algorithm.Algorithm;
import implementations.algorithm.AlgorithmNode;
import implementations.input.InputImp;
import interfaces.Conversion;
import interfaces.Input;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class TestAlgorithm {
	public static final String EXAMPLE_FILE = "test.dot";

	@Test
	public void testGenerateSchedule() {
		Input input = new InputImp(EXAMPLE_FILE, "2");
		Conversion conversion = new ConversionImp(input);
		Algorithm alg = new Algorithm(conversion.getDAG(), input.getProcessorCount());

//		List<List<AlgorithmNode>> schedules = alg.getSchedules();
//
//		assertEquals(384, schedules.size());
//
//		StringBuilder sb = new StringBuilder();
//		for (AlgorithmNode node : schedules.get(0)) {
//			sb.append(node.getNodeName() + node.getCore() + " ");
//		}
//
//		assertEquals("a0 b0 c0 d0", sb.toString().trim());
	}

	@Test
	public void testCalculateNormalSchedule() {
		Input input = new InputImp(EXAMPLE_FILE, "2");
		Conversion conversion = new ConversionImp(input);
		Algorithm alg = new Algorithm(conversion.getDAG(), input.getProcessorCount());

		List<AlgorithmNode> testCase = new ArrayList<>();
		AlgorithmNode a = new AlgorithmNode("a");
		AlgorithmNode b = new AlgorithmNode("b");
		AlgorithmNode c = new AlgorithmNode("c");
		AlgorithmNode d = new AlgorithmNode("d");
		a.setCore(1);
		b.setCore(1);
		c.setCore(2);
		d.setCore(2);
		testCase.add(a);
		testCase.add(b);
		testCase.add(c);
		testCase.add(d);

		SchedulerTime st = alg.calculateTotalTimeWrapper(testCase);

		assertEquals(st.getTotalTime(), 9);
		assertEquals(st.getNodeStartTime(0), 0);
		assertEquals(st.getNodeStartTime(1), 2);
		assertEquals(st.getNodeStartTime(2), 4);
		assertEquals(st.getNodeStartTime(3), 7);
	}

	@Test
	public void testValidSchedule() {
		Input input = new InputImp(EXAMPLE_FILE, "2");
		Conversion conversion = new ConversionImp(input);
		Algorithm alg = new Algorithm(conversion.getDAG(), input.getProcessorCount());

		//one assert is one valid/invalid schedule
		assertTrue(alg.checkValidScheduleWrapper(generateAlgorithmNodesForTesting(new String[]{"a", "b", "c", "d"})));

		assertTrue(alg.checkValidScheduleWrapper(generateAlgorithmNodesForTesting(new String[]{"a"})));

		assertFalse(alg.checkValidScheduleWrapper(generateAlgorithmNodesForTesting(new String[]{"b"})));

		assertFalse(alg.checkValidScheduleWrapper(generateAlgorithmNodesForTesting(new String[]{"d"})));

		assertFalse(alg.checkValidScheduleWrapper(generateAlgorithmNodesForTesting(new String[]{"a", "b", "d", "c"})));

		assertTrue(alg.checkValidScheduleWrapper(generateAlgorithmNodesForTesting(new String[]{"a", "c", "b", "d"})));

		assertFalse(alg.checkValidScheduleWrapper(generateAlgorithmNodesForTesting(new String[]{"a", "d", "b", "c"})));


	}

	/**
	 * Helper method for creating algorithm nodes for testing
	 * @param names
	 * @return
	 */
	private List<AlgorithmNode> generateAlgorithmNodesForTesting(String[] names){
		List<AlgorithmNode> nodes = new ArrayList<AlgorithmNode>();
		for(String name : names){
			AlgorithmNode algNode = new AlgorithmNode(name);
			nodes.add(algNode);
		}

		return nodes;
	}
}
