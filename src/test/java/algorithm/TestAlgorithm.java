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
	public static final String EXAMPLE_ISOLATED_NODE = "test2.dot";

//	@Test
//	public void testGenerateSchedule() {
//		Algorithm alg = computeAlgorithmFromInput(EXAMPLE_FILE, "2");
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
//	}

	/**
	 * Tests the check valid schedule function using Example file.
	 * The schedules listed in here should be invalid.
	 */
	@Test
	public void testInvalidSchedule1() {
		Algorithm alg = computeAlgorithmFromInput(EXAMPLE_FILE, "2");

		//one assert is one invalid schedule
		assertFalse(alg.checkValidScheduleWrapper(generateAlgorithmNodes(new String[]{"b"})));

		assertFalse(alg.checkValidScheduleWrapper(generateAlgorithmNodes(new String[]{"d"})));

		assertFalse(alg.checkValidScheduleWrapper(generateAlgorithmNodes(new String[]{"a", "b", "d", "c"})));

		assertFalse(alg.checkValidScheduleWrapper(generateAlgorithmNodes(new String[]{"a", "d", "b", "c"})));
	}
	
	/**
	 * Tests the check valid schedule function using Example file.
	 * The schedules listed in here should be valid.
	 * 
	 * At the same time, test to see if the calculate total time is working.
	 * 
	 * Test valid schedule 1 to 3 
	 */
	@Test
	public void testValidSchedule1() {
		Algorithm alg = computeAlgorithmFromInput(EXAMPLE_FILE, "2");
		List<AlgorithmNode> schedule1 = generateAlgorithmNodes(new String[]{"a", "b", "c", "d"});
		assertTrue(alg.checkValidScheduleWrapper(schedule1));
		setCoreForAlgorithmNodes(schedule1, new int[] {2, 2, 1, 1});
		SchedulerTime st = alg.calculateTotalTimeWrapper(schedule1);
		assertEquals(st.getTotalTime(), 9);
		assertEquals(st.getNodeStartTime(0), 0);
		assertEquals(st.getNodeStartTime(1), 2);
		assertEquals(st.getNodeStartTime(2), 4);
		assertEquals(st.getNodeStartTime(3), 7);
		
		assertTrue(alg.checkValidScheduleWrapper(generateAlgorithmNodes(new String[]{"a"})));
		
		assertTrue(alg.checkValidScheduleWrapper(generateAlgorithmNodes(new String[]{"a", "c", "b", "d"})));
	}

	/**
	 * Helper method for creating algorithm nodes for testing
	 * @param names
	 * @return
	 */
	private List<AlgorithmNode> generateAlgorithmNodes(String[] names){
		List<AlgorithmNode> nodes = new ArrayList<AlgorithmNode>();
		for(String name : names){
			AlgorithmNode algNode = new AlgorithmNode(name);
			nodes.add(algNode);
		}

		return nodes;
	}
	
	/**
	 * Helper method for creating algorithm nodes for testing with core number
	 * @param names
	 * @param core
	 * @return
	 */
	private void setCoreForAlgorithmNodes(List<AlgorithmNode> nodes, int[] cores){
		int[] index = {0}; //only one element. (int index won't work for lambda...)
		nodes.forEach(n -> {
			n.setCore(cores[index[0]]);
			index[0]++;
		});
		
	}
	
	/**
	 * Helper method for getting the algorithm object from a file.
	 * (By doing so, also executes all the main algorithm logic)
	 * @param path : path of the graph .dot file.
	 * @param core : number of cores.
	 */
	private Algorithm computeAlgorithmFromInput (String path, String core){
		Input input = new InputImp(path, core);
		Conversion conversion = new ConversionImp(input);
		return new Algorithm(conversion.getDAG(), input.getProcessorCount());
	}
}
