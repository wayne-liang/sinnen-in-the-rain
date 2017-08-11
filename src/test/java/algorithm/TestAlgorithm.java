package algorithm;

import implementations.ConversionImp;
import implementations.SchedulerTime;
import implementations.algorithm.Algorithm;
import implementations.algorithm.AlgorithmNode;
import implementations.io.InputImp;
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

	
	//===================TEST_FILE1====================//
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
	 * Each test method represents a schedule (but can have different core arrangements)
	 * 
	 * Example File 1: Test methods 1-3.
	 */
	@Test
	public void testValidSchedule1() {
		Algorithm alg = computeAlgorithmFromInput(EXAMPLE_FILE, "2");
		List<AlgorithmNode> schedule = generateAlgorithmNodes(new String[]{"a", "b", "c", "d"});
		assertTrue(alg.checkValidScheduleWrapper(schedule));
		setCoreForAlgorithmNodes(schedule, new int[] {2, 2, 1, 1});
		
		SchedulerTime st = alg.calculateTotalTimeWrapper(schedule);
		assertEquals(9, st.getTotalTime());
		
		int [] expectedResults = new int[] {0, 2, 4, 7};
		for (int i=0; i<schedule.size(); i++){
			assertEquals(expectedResults[i], st.getNodeStartTime(i));
		}
		
	}
	
	@Test
	public void testValidSchedule2() {
		Algorithm alg = computeAlgorithmFromInput(EXAMPLE_FILE, "2");
		List<AlgorithmNode> schedule = generateAlgorithmNodes(new String[]{"a"});
		assertTrue(alg.checkValidScheduleWrapper(schedule));
		setCoreForAlgorithmNodes(schedule, new int[] {2});
		
		SchedulerTime st = alg.calculateTotalTimeWrapper(schedule);
		assertEquals(st.getTotalTime(), 2);
		assertEquals(st.getNodeStartTime(0), 0);
	}
	
	@Test
	public void testValidSchedule3() {
		Algorithm alg = computeAlgorithmFromInput(EXAMPLE_FILE, "2");
		List<AlgorithmNode> schedule = generateAlgorithmNodes(new String[]{"a", "c", "b", "d"});
		assertTrue(alg.checkValidScheduleWrapper(schedule));
		setCoreForAlgorithmNodes(schedule, new int[] {1, 1, 1, 1});
		
		SchedulerTime st = alg.calculateTotalTimeWrapper(schedule);
		assertEquals(10, st.getTotalTime());
		
		int [] expectedResults = new int[] {0, 2, 5, 8};
		for (int i=0; i<schedule.size(); i++){
			assertEquals(expectedResults[i], st.getNodeStartTime(i));
		}
	}
	
	
	//===================TEST_FILE2====================//
	/**
	 * Tests the check valid schedule function using Example file 2 (modified to remove bd arc and add node e).
	 * The schedules listed in here should be invalid.
	 */
	@Test
	public void testInvalidSchedule2() {
		Algorithm alg = computeAlgorithmFromInput(EXAMPLE_ISOLATED_NODE, "2");

		//one assert is one invalid schedule

		assertFalse(alg.checkValidScheduleWrapper(generateAlgorithmNodes(new String[]{"b"})));

		assertFalse(alg.checkValidScheduleWrapper(generateAlgorithmNodes(new String[]{"a", "b", "d", "e", "c"})));

		assertFalse(alg.checkValidScheduleWrapper(generateAlgorithmNodes(new String[]{"a", "b", "d", "c", "e"})));
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
		Algorithm alg = computeAlgorithmFromInput(EXAMPLE_ISOLATED_NODE, "2");
		List<AlgorithmNode> schedule = generateAlgorithmNodes(new String[]{"e", "a", "b", "c", "d"});
		assertTrue(alg.checkValidScheduleWrapper(schedule));
		//1
		setCoreForAlgorithmNodes(schedule, new int[] {1, 1, 1, 1, 1});
		
		SchedulerTime st = alg.calculateTotalTimeWrapper(schedule);
		assertEquals(14, st.getTotalTime());
		
		int [] expectedResults = new int[] {0, 4, 6, 9, 12};
		for (int i=0; i<schedule.size(); i++){
			assertEquals(expectedResults[i], st.getNodeStartTime(i));
		}
		
		//2
		setCoreForAlgorithmNodes(schedule, new int[] {2, 1, 1, 1, 1});
		
		SchedulerTime st2 = alg.calculateTotalTimeWrapper(schedule);
		assertEquals(10, st2.getTotalTime());
		
		int [] expectedResults2 = new int[] {0, 0, 2, 5, 8};
		for (int i=0; i<schedule.size(); i++){
			assertEquals(expectedResults2[i], st2.getNodeStartTime(i));
		}
		
		//3
		setCoreForAlgorithmNodes(schedule, new int[] {2, 1, 1, 2, 2});
		
		SchedulerTime st3 = alg.calculateTotalTimeWrapper(schedule);
		assertEquals(9, st3.getTotalTime());
		
		int [] expectedResults3 = new int[] {0, 0, 2, 4, 7};
		for (int i=0; i<schedule.size(); i++){
			assertEquals(expectedResults3[i], st3.getNodeStartTime(i));
		}
		
		//4
		setCoreForAlgorithmNodes(schedule, new int[] {2, 1, 1, 2, 1});
		
		SchedulerTime st4 = alg.calculateTotalTimeWrapper(schedule);
		assertEquals(10, st4.getTotalTime());
		
		int [] expectedResults4 = new int[] {0, 0, 2, 4, 8};
		for (int i=0; i<schedule.size(); i++){
			assertEquals(expectedResults4[i], st4.getNodeStartTime(i));
		}
	}
	
	@Test
	public void testValidSchedule5() {
		Algorithm alg = computeAlgorithmFromInput(EXAMPLE_ISOLATED_NODE, "2");
		List<AlgorithmNode> schedule = generateAlgorithmNodes(new String[]{"a", "e", "b", "c", "d"});
		assertTrue(alg.checkValidScheduleWrapper(schedule));
		//1
		setCoreForAlgorithmNodes(schedule, new int[] {1, 1, 1, 1, 1});
		
		SchedulerTime st = alg.calculateTotalTimeWrapper(schedule);
		assertEquals(14, st.getTotalTime());
		
		int [] expectedResults = new int[] {0, 2, 6, 9, 12};
		for (int i=0; i<schedule.size(); i++){
			assertEquals(expectedResults[i], st.getNodeStartTime(i));
		}	
	}
	
	@Test
	public void testValidSchedule6() {
		Algorithm alg = computeAlgorithmFromInput(EXAMPLE_ISOLATED_NODE, "2");
		List<AlgorithmNode> schedule = generateAlgorithmNodes(new String[]{"a", "b", "e"});
		assertTrue(alg.checkValidScheduleWrapper(schedule));
		//1
		setCoreForAlgorithmNodes(schedule, new int[] {1, 2, 1});
		
		SchedulerTime st = alg.calculateTotalTimeWrapper(schedule);
		assertEquals(6, st.getTotalTime());
		
		int [] expectedResults = new int[] {0, 3, 2};
		for (int i=0; i<schedule.size(); i++){
			assertEquals(expectedResults[i], st.getNodeStartTime(i));
		}	
		
		//2
		setCoreForAlgorithmNodes(schedule, new int[] {1, 1, 2});
		
		SchedulerTime st2 = alg.calculateTotalTimeWrapper(schedule);
		assertEquals(5, st2.getTotalTime());
		
		int [] expectedResults2 = new int[] {0, 2, 0};
		for (int i=0; i<schedule.size(); i++){
			assertEquals(expectedResults2[i], st2.getNodeStartTime(i));
		}	
	}
	
	@Test
	public void testValidSchedule7() {
		Algorithm alg = computeAlgorithmFromInput(EXAMPLE_ISOLATED_NODE, "2");
		List<AlgorithmNode> schedule = generateAlgorithmNodes(new String[]{"a", "b", "c", "d", "e"});
		assertTrue(alg.checkValidScheduleWrapper(schedule));
		//1
		setCoreForAlgorithmNodes(schedule, new int[] {1, 1, 1, 1, 2});
		
		SchedulerTime st = alg.calculateTotalTimeWrapper(schedule);
		assertEquals(10, st.getTotalTime());
		
		int [] expectedResults = new int[] {0, 2, 5, 8, 0};
		for (int i=0; i<schedule.size(); i++){
			assertEquals(expectedResults[i], st.getNodeStartTime(i));
		}	
		
		//2
		setCoreForAlgorithmNodes(schedule, new int[] {1, 2, 1, 1, 2});
		
		SchedulerTime st2 = alg.calculateTotalTimeWrapper(schedule);
		assertEquals(10, st.getTotalTime());
		
		int [] expectedResults2 = new int[] {0, 3, 2, 5, 6};
		for (int i=0; i<schedule.size(); i++){
			assertEquals(expectedResults2[i], st2.getNodeStartTime(i));
		}	
	}
	
	@Test
	public void testValidSchedule8() {
		Algorithm alg = computeAlgorithmFromInput(EXAMPLE_ISOLATED_NODE, "2");
		List<AlgorithmNode> schedule = generateAlgorithmNodes(new String[]{"e"});
		assertTrue(alg.checkValidScheduleWrapper(schedule));
		//1
		setCoreForAlgorithmNodes(schedule, new int[] {2});
		
		SchedulerTime st = alg.calculateTotalTimeWrapper(schedule);
		assertEquals(4, st.getTotalTime());
		
		int [] expectedResults = new int[] {0};
		for (int i=0; i<schedule.size(); i++){
			assertEquals(expectedResults[i], st.getNodeStartTime(i));
		}	
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
