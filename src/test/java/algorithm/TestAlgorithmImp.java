package algorithm;

import implementations.algorithm.AlgorithmImp;
import implementations.algorithm.AlgorithmNodeImp;
import interfaces.algorithm.AlgorithmNode;
import implementations.io.Conversion;
import implementations.io.InputImp;
import implementations.structures.ScheduleImp;
import interfaces.io.Input;
import interfaces.structures.Schedule;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

public class TestAlgorithmImp {
	public static final String EXAMPLE_FILE = "test.dot";
	public static final String EXAMPLE_ISOLATED_NODE = "test2.dot";
	public static final String EXAMPLE_NON_ISOLATED_NODE_E = "test3.dot";

//	@Test
//	public void testGenerateSchedule() {
//		AlgorithmImp alg = computeAlgorithmFromInput(EXAMPLE_FILE, "2");
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


	//===================TEST_FILE1====================//
	/**
	 * Tests the check valid schedule function using Example file.
	 * The schedules listed in here should be invalid.
	 */
	@Test
	public void testInvalidSchedule1() {
		AlgorithmImp alg = computeAlgorithmFromInput(EXAMPLE_FILE, "2");

		//one assert is one invalid schedule
		//invalid because its starting node is not the DAG starting node "a"//
		assertFalse(alg.checkValidScheduleWrapper(generateAlgorithmNodes(new String[]{"b"})));

		assertFalse(alg.checkValidScheduleWrapper(generateAlgorithmNodes(new String[]{"d"})));

		// invalid because "d" cannot be processed before the sucessors "b", "c" are finished.//
		assertFalse(alg.checkValidScheduleWrapper(generateAlgorithmNodes(new String[]{"a", "b", "d"})));

		assertFalse(alg.checkValidScheduleWrapper(generateAlgorithmNodes(new String[]{"a", "d"})));
	}
	
	/**
	 * Tests the check valid schedule function using Example file.
	 * The schedules listed in here should be valid.
	 * 
	 * At the same time, test to see if the calculate total time is working.
	 * 
	 * Each test method represents a schedule (but can have different core arrangements)
	 *
	 * Example File 1: Test methods 1-3.
	 */
	@Test
	public void testValidSchedule1() {
		AlgorithmImp alg = computeAlgorithmFromInput(EXAMPLE_FILE, "2");
		List<AlgorithmNode> schedule = generateAlgorithmNodes(new String[]{"a", "b", "c", "d"});
		assertTrue(alg.checkValidScheduleWrapper(schedule));
		setCoreForAlgorithmNodes(schedule, new int[] {2, 2, 1, 1});

		Schedule st = alg.calculateTotalTimeWrapper(schedule);
		assertEquals(9, st.getTotalTime());

		int [] expectedResults = new int[] {0, 2, 4, 7};
		for (int i=0; i<schedule.size(); i++){
			assertEquals(expectedResults[i], st.getNodeStartTime(i));
		}
	}
	
	/**
	 * New: uses the latest Schedule implementation (has memorization)
	 */
	@Test
	public void testValidSchedule1New() {
		AlgorithmImp alg = computeAlgorithmFromInput(EXAMPLE_FILE, "2");
		String[] namesOfNodes = new String[] {"a", "b", "c", "d"};
		int [] coresOfNodes = new int[] {2, 2, 1, 1};
		
		List<AlgorithmNode> nodesList = generateAlgorithmNodes(namesOfNodes);
		assertTrue(alg.checkValidScheduleWrapper(nodesList));
		setCoreForAlgorithmNodes(nodesList, coresOfNodes);

		Schedule schedule = new ScheduleImp (2);
		for (int i = 0; i<namesOfNodes.length; i++) {
			AlgorithmNode nextNode = new AlgorithmNodeImp(namesOfNodes[i]);
			nextNode.setCore(coresOfNodes[i]);
			schedule = schedule.getNextSchedule(nextNode);
		}

		assertEquals(9, schedule.getTotalTime());

		int [] expectedResults = new int[] {0, 2, 4, 7};
		for (int i=0; i<nodesList.size(); i++){
			assertEquals(expectedResults[i], schedule.getNodeStartTime(i));
		}
	}

	/*
	Test checkValidScheduleWrapper() with non-isolated e
	 */
	@Test
	public void	checkValidScheduleWrapper() {
		AlgorithmImp alg = computeAlgorithmFromInput(EXAMPLE_NON_ISOLATED_NODE_E, "2");

		//one assert is one invalid schedule
		//invalid because its starting node is not the DAG starting node "a"//
		assertTrue(alg.checkValidScheduleWrapper(generateAlgorithmNodes(new String[]{"e", "a", "b", "c"})));

		assertTrue(alg.checkValidScheduleWrapper(generateAlgorithmNodes(new String[]{"a", "e", "b", "c"})));

		// invalid, because c requires e to be finished first
		//	Removed "e".
		assertFalse(alg.checkValidScheduleWrapper(generateAlgorithmNodes(new String[]{"a", "b", "c"})));

		//test null, it should return invalid
		assertFalse(alg.checkValidScheduleWrapper(null));
	}

	@Test
	public void testValidSchedule2() {
		AlgorithmImp alg = computeAlgorithmFromInput(EXAMPLE_FILE, "2");
		List<AlgorithmNode> schedule = generateAlgorithmNodes(new String[]{"a"});
		assertTrue(alg.checkValidScheduleWrapper(schedule));
		setCoreForAlgorithmNodes(schedule, new int[] {2});

		ScheduleImp st = alg.calculateTotalTimeWrapper(schedule);
		assertEquals(2, st.getTotalTime());
		assertEquals(0, st.getNodeStartTime(0));
	}
	
	/**
	 * New: uses the latest Schedule implementation (has memorization)
	 */
	@Test
	public void testValidSchedule2New() {
		AlgorithmImp alg = computeAlgorithmFromInput(EXAMPLE_FILE, "2");
		String[] namesOfNodes = new String[] {"a"};
		int [] coresOfNodes = new int[] {2};
		
		List<AlgorithmNode> nodesList = generateAlgorithmNodes(namesOfNodes);
		assertTrue(alg.checkValidScheduleWrapper(nodesList));
		setCoreForAlgorithmNodes(nodesList, coresOfNodes);

		Schedule schedule = new ScheduleImp (2);
		for (int i = 0; i<namesOfNodes.length; i++) {
			AlgorithmNode nextNode = new AlgorithmNodeImp(namesOfNodes[i]);
			nextNode.setCore(coresOfNodes[i]);
			schedule = schedule.getNextSchedule(nextNode);
		}

		assertEquals(2, schedule.getTotalTime());

		int [] expectedResults = new int[] {0};
		for (int i=0; i<nodesList.size(); i++){
			assertEquals(expectedResults[i], schedule.getNodeStartTime(i));
		}
	}

	@Test
	public void testValidSchedule3() {
		AlgorithmImp alg = computeAlgorithmFromInput(EXAMPLE_FILE, "2");
		List<AlgorithmNode> schedule = generateAlgorithmNodes(new String[]{"a", "c", "b", "d"});
		assertTrue(alg.checkValidScheduleWrapper(schedule));
		setCoreForAlgorithmNodes(schedule, new int[] {1, 1, 1, 1});
		
		ScheduleImp st = alg.calculateTotalTimeWrapper(schedule);
		assertEquals(10, st.getTotalTime());
		
		int [] expectedResults = new int[] {0, 2, 5, 8};
		for (int i=0; i<schedule.size(); i++){
			assertEquals(expectedResults[i], st.getNodeStartTime(i));
		}
	}
	
	/**
	 * New: uses the latest Schedule implementation (has memorization)
	 */
	@Test
	public void testValidSchedule3New() {
		AlgorithmImp alg = computeAlgorithmFromInput(EXAMPLE_FILE, "2");
		String[] namesOfNodes = new String[] {"a", "c", "b", "d"};
		int [] coresOfNodes = new int[] {1, 1, 1, 1};
		
		List<AlgorithmNode> nodesList = generateAlgorithmNodes(namesOfNodes);
		assertTrue(alg.checkValidScheduleWrapper(nodesList));

		Schedule schedule = new ScheduleImp (2);
		for (int i = 0; i<namesOfNodes.length; i++) {
			AlgorithmNode nextNode = new AlgorithmNodeImp(namesOfNodes[i]);
			nextNode.setCore(coresOfNodes[i]);
			schedule = schedule.getNextSchedule(nextNode);
		}

		assertEquals(10, schedule.getTotalTime());

		int [] expectedResults = new int[] {0, 2, 5, 8};
		for (int i=0; i<nodesList.size(); i++){
			assertEquals(expectedResults[i], schedule.getNodeStartTime(i));
		}
	}


	//===================TEST_FILE2====================//
	/**
	 * Tests the check valid schedule function using Example file 2 (modified to remove arc bd and add node e).
	 * The schedules listed in here should be invalid.
	 */
	@Test
	public void testInvalidSchedule2() {
		AlgorithmImp alg = computeAlgorithmFromInput(EXAMPLE_ISOLATED_NODE, "2");

		//one assert is one invalid schedule
		//invalid because its starting node is not the DAG starting node "a"//
		assertFalse(alg.checkValidScheduleWrapper(generateAlgorithmNodes(new String[]{"b"})));

		// invalid because "d" cannot be processed before the sucessors "b", "c" are finished.//
		assertFalse(alg.checkValidScheduleWrapper(generateAlgorithmNodes(new String[]{"a", "b", "d"})));
	}

	/**
	 * Tests the check valid schedule function using Example file.
	 * The schedules listed in here should be valid.
	 *
	 * At the same time, test to see if the calculate total time is working.
	 *
	 * Each test method represents a schedule (but can have different core arrangements)
	 *
	 * Example File 2: Test methods 4-8.
	 */
	@Test
	public void testValidSchedule4() {
		AlgorithmImp alg = computeAlgorithmFromInput(EXAMPLE_ISOLATED_NODE, "2");
		List<AlgorithmNode> schedule = generateAlgorithmNodes(new String[]{"e", "a", "b", "c", "d"});
		assertTrue(alg.checkValidScheduleWrapper(schedule));
		//1
		setCoreForAlgorithmNodes(schedule, new int[] {1, 1, 1, 1, 1});

		ScheduleImp st = alg.calculateTotalTimeWrapper(schedule);
		assertEquals(14, st.getTotalTime());

		int [] expectedResults = new int[] {0, 4, 6, 9, 12};
		for (int i=0; i<schedule.size(); i++){
			assertEquals(expectedResults[i], st.getNodeStartTime(i));
		}

		//2
		setCoreForAlgorithmNodes(schedule, new int[] {2, 1, 1, 1, 1});

		ScheduleImp st2 = alg.calculateTotalTimeWrapper(schedule);
		assertEquals(10, st2.getTotalTime());

		int [] expectedResults2 = new int[] {0, 0, 2, 5, 8};
		for (int i=0; i<schedule.size(); i++){
			assertEquals(expectedResults2[i], st2.getNodeStartTime(i));
		}

		//3
		setCoreForAlgorithmNodes(schedule, new int[] {2, 1, 1, 2, 2});

		ScheduleImp st3 = alg.calculateTotalTimeWrapper(schedule);
		assertEquals(9, st3.getTotalTime());

		int [] expectedResults3 = new int[] {0, 0, 2, 4, 7};
		for (int i=0; i<schedule.size(); i++){
			assertEquals(expectedResults3[i], st3.getNodeStartTime(i));
		}

		//4
		setCoreForAlgorithmNodes(schedule, new int[] {2, 1, 1, 2, 1});

		ScheduleImp st4 = alg.calculateTotalTimeWrapper(schedule);
		assertEquals(10, st4.getTotalTime());

		int [] expectedResults4 = new int[] {0, 0, 2, 4, 8};
		for (int i=0; i<schedule.size(); i++){
			assertEquals(expectedResults4[i], st4.getNodeStartTime(i));
		}
	}
	
	/**
	 * New: uses the latest Schedule implementation (has memorization)
	 */
	@Test
	public void testValidSchedule4New() {
		AlgorithmImp alg = computeAlgorithmFromInput(EXAMPLE_FILE, "2");
		String[] namesOfNodes = new String[] {"e", "a", "b", "c", "d"};
		int [] coresOfNodes = new int[] {1, 1, 1, 1, 1};
		
		List<AlgorithmNode> nodesList = generateAlgorithmNodes(namesOfNodes);
		assertTrue(alg.checkValidScheduleWrapper(nodesList));

		//1
		setCoreForAlgorithmNodes(nodesList, coresOfNodes);
		Schedule schedule = new ScheduleImp (2);
		for (int i = 0; i<namesOfNodes.length; i++) {
			AlgorithmNode nextNode = new AlgorithmNodeImp(namesOfNodes[i]);
			nextNode.setCore(coresOfNodes[i]);
			schedule = schedule.getNextSchedule(nextNode);
		}

		assertEquals(14, schedule.getTotalTime());

		int [] expectedResults = new int[] {0, 4, 6, 9, 12};
		for (int i=0; i<nodesList.size(); i++){
			assertEquals(expectedResults[i], schedule.getNodeStartTime(i));
		}
		
		//2
		int [] coresOfNodes2 = new int[] {2, 1, 1, 1, 1};
		
		Schedule schedule2 = new ScheduleImp (2);
		for (int i = 0; i<namesOfNodes.length; i++) {
			AlgorithmNode nextNode = new AlgorithmNodeImp(namesOfNodes[i]);
			nextNode.setCore(coresOfNodes2[i]);
			schedule2 = schedule2.getNextSchedule(nextNode);
		}

		assertEquals(10, schedule2.getTotalTime());

		int [] expectedResults2 = new int[] {0, 0, 2, 5, 8};
		for (int i=0; i<nodesList.size(); i++){
			assertEquals(expectedResults2[i], schedule2.getNodeStartTime(i));
		}
		
		//3
		int [] coresOfNodes3 = new int[] {2, 1, 1, 2, 2};
		
		Schedule schedule3 = new ScheduleImp (2);
		for (int i = 0; i<namesOfNodes.length; i++) {
			AlgorithmNode nextNode = new AlgorithmNodeImp(namesOfNodes[i]);
			nextNode.setCore(coresOfNodes3[i]);
			schedule3 = schedule3.getNextSchedule(nextNode);
		}

		assertEquals(9, schedule3.getTotalTime());

		int [] expectedResults3 = new int[] {0, 0, 2, 4, 7};
		for (int i=0; i<nodesList.size(); i++){
			assertEquals(expectedResults3[i], schedule3.getNodeStartTime(i));
		}
		
		//4
		int [] coresOfNodes4 = new int[] {2, 1, 1, 2, 1};
		
		Schedule schedule4 = new ScheduleImp (2);
		for (int i = 0; i<namesOfNodes.length; i++) {
			AlgorithmNode nextNode = new AlgorithmNodeImp(namesOfNodes[i]);
			nextNode.setCore(coresOfNodes4[i]);
			schedule4 = schedule4.getNextSchedule(nextNode);
		}

		assertEquals(10, schedule4.getTotalTime());

		int [] expectedResults4 = new int[] {0, 0, 2, 4, 8};
		for (int i=0; i<nodesList.size(); i++){
			assertEquals(expectedResults4[i], schedule4.getNodeStartTime(i));
		}
	}

	@Test
	public void testValidSchedule5() {
		AlgorithmImp alg = computeAlgorithmFromInput(EXAMPLE_ISOLATED_NODE, "2");
		List<AlgorithmNode> schedule = generateAlgorithmNodes(new String[]{"a", "e", "b", "c", "d"});
		assertTrue(alg.checkValidScheduleWrapper(schedule));
		//1
		setCoreForAlgorithmNodes(schedule, new int[] {1, 1, 1, 1, 1});

		ScheduleImp st = alg.calculateTotalTimeWrapper(schedule);
		assertEquals(14, st.getTotalTime());

		int [] expectedResults = new int[] {0, 2, 6, 9, 12};
		for (int i=0; i<schedule.size(); i++){
			assertEquals(expectedResults[i], st.getNodeStartTime(i));
		}
	}
	
	/**
	 * New: uses the latest Schedule implementation (has memorization)
	 */
	@Test
	public void testValidSchedule5New() {
		AlgorithmImp alg = computeAlgorithmFromInput(EXAMPLE_ISOLATED_NODE, "2");
		String[] namesOfNodes = new String[] {"a", "e", "b", "c", "d"};
		int [] coresOfNodes = new int[] {1, 1, 1, 1, 1};
		
		List<AlgorithmNode> nodesList = generateAlgorithmNodes(namesOfNodes);
		assertTrue(alg.checkValidScheduleWrapper(nodesList));

		Schedule schedule = new ScheduleImp (2);
		for (int i = 0; i<namesOfNodes.length; i++) {
			AlgorithmNode nextNode = new AlgorithmNodeImp(namesOfNodes[i]);
			nextNode.setCore(coresOfNodes[i]);
			schedule = schedule.getNextSchedule(nextNode);
		}

		assertEquals(14, schedule.getTotalTime());

		int [] expectedResults = new int[] {0, 2, 6, 9, 12};
		for (int i=0; i<nodesList.size(); i++){
			assertEquals(expectedResults[i], schedule.getNodeStartTime(i));
		}
	}

	@Test
	public void testValidSchedule6() {
		AlgorithmImp alg = computeAlgorithmFromInput(EXAMPLE_ISOLATED_NODE, "2");
		List<AlgorithmNode> schedule = generateAlgorithmNodes(new String[]{"a", "b", "e"});
		assertTrue(alg.checkValidScheduleWrapper(schedule));
		//1
		setCoreForAlgorithmNodes(schedule, new int[] {1, 2, 1});

		ScheduleImp st = alg.calculateTotalTimeWrapper(schedule);
		assertEquals(6, st.getTotalTime());

		int [] expectedResults = new int[] {0, 3, 2};
		for (int i=0; i<schedule.size(); i++){
			assertEquals(expectedResults[i], st.getNodeStartTime(i));
		}

		//2
		setCoreForAlgorithmNodes(schedule, new int[] {1, 1, 2});

		ScheduleImp st2 = alg.calculateTotalTimeWrapper(schedule);
		assertEquals(5, st2.getTotalTime());

		int [] expectedResults2 = new int[] {0, 2, 0};
		for (int i=0; i<schedule.size(); i++){
			assertEquals(expectedResults2[i], st2.getNodeStartTime(i));
		}
	}
	
	/**
	 * New: uses the latest Schedule implementation (has memorization)
	 */
	@Test
	public void testValidSchedule6New() {
		AlgorithmImp alg = computeAlgorithmFromInput(EXAMPLE_FILE, "2");
		String[] namesOfNodes = new String[] {"a", "b", "e"};
		int [] coresOfNodes = new int[] {1, 2, 1};
		
		List<AlgorithmNode> nodesList = generateAlgorithmNodes(namesOfNodes);
		assertTrue(alg.checkValidScheduleWrapper(nodesList));

		//1
		setCoreForAlgorithmNodes(nodesList, coresOfNodes);
		Schedule schedule = new ScheduleImp (2);
		for (int i = 0; i<namesOfNodes.length; i++) {
			AlgorithmNode nextNode = new AlgorithmNodeImp(namesOfNodes[i]);
			nextNode.setCore(coresOfNodes[i]);
			schedule = schedule.getNextSchedule(nextNode);
		}

		assertEquals(6, schedule.getTotalTime());

		int [] expectedResults = new int[] {0, 3, 2};
		for (int i=0; i<nodesList.size(); i++){
			assertEquals(expectedResults[i], schedule.getNodeStartTime(i));
		}
		
		//2
		int [] coresOfNodes2 = new int[] {1, 1, 2};
		
		Schedule schedule2 = new ScheduleImp (2);
		for (int i = 0; i<namesOfNodes.length; i++) {
			AlgorithmNode nextNode = new AlgorithmNodeImp(namesOfNodes[i]);
			nextNode.setCore(coresOfNodes2[i]);
			schedule2 = schedule2.getNextSchedule(nextNode);
		}

		assertEquals(5, schedule2.getTotalTime());

		int [] expectedResults2 = new int[] {0, 2, 0};
		for (int i=0; i<nodesList.size(); i++){
			assertEquals(expectedResults2[i], schedule2.getNodeStartTime(i));
		}
	}

	@Test
	public void testValidSchedule7() {
		AlgorithmImp alg = computeAlgorithmFromInput(EXAMPLE_ISOLATED_NODE, "2");
		List<AlgorithmNode> schedule = generateAlgorithmNodes(new String[]{"a", "b", "c", "d", "e"});
		assertTrue(alg.checkValidScheduleWrapper(schedule));
		//1
		setCoreForAlgorithmNodes(schedule, new int[] {1, 1, 1, 1, 2});

		ScheduleImp st = alg.calculateTotalTimeWrapper(schedule);
		assertEquals(10, st.getTotalTime());

		int [] expectedResults = new int[] {0, 2, 5, 8, 0};
		for (int i=0; i<schedule.size(); i++){
			assertEquals(expectedResults[i], st.getNodeStartTime(i));
		}

		//2
		setCoreForAlgorithmNodes(schedule, new int[] {1, 2, 1, 1, 2});

        ScheduleImp st2 = alg.calculateTotalTimeWrapper(schedule);
		assertEquals(10, st.getTotalTime());

		int [] expectedResults2 = new int[] {0, 3, 2, 5, 6};
		for (int i=0; i<schedule.size(); i++){
			assertEquals(expectedResults2[i], st2.getNodeStartTime(i));
		}
	}
	
	/**
	 * New: uses the latest Schedule implementation (has memorization)
	 */
	@Test
	public void testValidSchedule7New() {
		AlgorithmImp alg = computeAlgorithmFromInput(EXAMPLE_ISOLATED_NODE, "2");
		String[] namesOfNodes = new String[] {"a", "b", "c", "d", "e"};
		int [] coresOfNodes = new int[] {1, 1, 1, 1, 2};
		
		List<AlgorithmNode> nodesList = generateAlgorithmNodes(namesOfNodes);
		assertTrue(alg.checkValidScheduleWrapper(nodesList));

		//1
		setCoreForAlgorithmNodes(nodesList, coresOfNodes);
		Schedule schedule = new ScheduleImp (2);
		for (int i = 0; i<namesOfNodes.length; i++) {
			AlgorithmNode nextNode = new AlgorithmNodeImp(namesOfNodes[i]);
			nextNode.setCore(coresOfNodes[i]);
			schedule = schedule.getNextSchedule(nextNode);
		}

		assertEquals(10, schedule.getTotalTime());

		int [] expectedResults = new int[] {0, 2, 5, 8, 0};
		for (int i=0; i<nodesList.size(); i++){
			assertEquals(expectedResults[i], schedule.getNodeStartTime(i));
		}
		
		//2
		int [] coresOfNodes2 = new int[] {1, 2, 1, 1, 2};
		
		Schedule schedule2 = new ScheduleImp (2);
		for (int i = 0; i<namesOfNodes.length; i++) {
			AlgorithmNode nextNode = new AlgorithmNodeImp(namesOfNodes[i]);
			nextNode.setCore(coresOfNodes2[i]);
			schedule2 = schedule2.getNextSchedule(nextNode);
		}

		assertEquals(10, schedule2.getTotalTime());

		int [] expectedResults2 = new int[] {0, 3, 2, 5, 6};
		for (int i=0; i<nodesList.size(); i++){
			assertEquals(expectedResults2[i], schedule2.getNodeStartTime(i));
		}
	}

	@Test
	public void testValidSchedule8() {
		AlgorithmImp alg = computeAlgorithmFromInput(EXAMPLE_ISOLATED_NODE, "2");
		List<AlgorithmNode> schedule = generateAlgorithmNodes(new String[]{"e"});
		assertTrue(alg.checkValidScheduleWrapper(schedule));
		//1
		setCoreForAlgorithmNodes(schedule, new int[] {2});

        ScheduleImp st = alg.calculateTotalTimeWrapper(schedule);
		assertEquals(4, st.getTotalTime());

		int [] expectedResults = new int[] {0};
		for (int i=0; i<schedule.size(); i++){
			assertEquals(expectedResults[i], st.getNodeStartTime(i));
		}
	}
	
	/**
	 * New: uses the latest Schedule implementation (has memorization)
	 */
	@Test
	public void testValidSchedule8New() {
		AlgorithmImp alg = computeAlgorithmFromInput(EXAMPLE_ISOLATED_NODE, "2");
		String[] namesOfNodes = new String[] {"e"};
		int [] coresOfNodes = new int[] {2};
		
		List<AlgorithmNode> nodesList = generateAlgorithmNodes(namesOfNodes);
		assertTrue(alg.checkValidScheduleWrapper(nodesList));

		Schedule schedule = new ScheduleImp (2);
		for (int i = 0; i<namesOfNodes.length; i++) {
			AlgorithmNode nextNode = new AlgorithmNodeImp(namesOfNodes[i]);
			nextNode.setCore(coresOfNodes[i]);
			schedule = schedule.getNextSchedule(nextNode);
		}

		assertEquals(4, schedule.getTotalTime());

		int [] expectedResults = new int[] {0};
		for (int i=0; i<nodesList.size(); i++){
			assertEquals(expectedResults[i], schedule.getNodeStartTime(i));
		}
	}


	/**
	 * Helper method for creating algorithm nodes for testing
	 * @param names
	 * @return
	 */
	private List<AlgorithmNode> generateAlgorithmNodes(String[] names) {
		List<AlgorithmNode> nodes = new ArrayList<AlgorithmNode>();
		for(String name : names){
			AlgorithmNode algNode = new AlgorithmNodeImp(name);
			nodes.add(algNode);
		}

		return nodes;
	}
	
	/**
	 * Helper method for creating algorithm nodes for testing with core number
	 * @param nodes
	 * @param cores
	 * @return
	 */
	private void setCoreForAlgorithmNodes(List<AlgorithmNode> nodes, int[] cores) {
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
	private AlgorithmImp computeAlgorithmFromInput(String path, String core) {
		Input input = new InputImp(path, core);
		Conversion conversion = new Conversion(input);
		return new AlgorithmImp(input.getProcessorCount(),false,1);
	}
}