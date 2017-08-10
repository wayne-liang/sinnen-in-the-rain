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

public class TestAlgorithm {
	public static final String FILENAME = "test.dot";

	@Test
	public void testGenerateSchedule() {
		Input input = new InputImp(FILENAME, "2");
		Conversion conversion = new ConversionImp(input);
		Algorithm alg = new Algorithm(conversion.getDAG(), input.getProcessorCount());

		List<List<AlgorithmNode>> schedules = alg.getSchedules();

		assertEquals(384, schedules.size());

		StringBuilder sb = new StringBuilder();
		for (AlgorithmNode node : schedules.get(0)) {
			sb.append(node.getNodeName() + node.getCore() + " ");
		}

		assertEquals("a0 b0 c0 d0", sb.toString().trim());
	}

	@Test
	public void testCalculateNormalSchedule() {
		Input input = new InputImp(FILENAME, "2");
		Conversion conversion = new ConversionImp(input);
		Algorithm alg = new Algorithm(conversion.getDAG(), input.getProcessorCount());

		List<AlgorithmNode> testCase = new ArrayList<>();
		AlgorithmNode a = new AlgorithmNode("a");
		AlgorithmNode b = new AlgorithmNode("b");
		AlgorithmNode c = new AlgorithmNode("c");
		AlgorithmNode d = new AlgorithmNode("d");
		a.setCore(0);
		b.setCore(0);
		c.setCore(1);
		d.setCore(1);
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
}
