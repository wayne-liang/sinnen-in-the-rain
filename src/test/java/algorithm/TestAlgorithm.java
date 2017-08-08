package algorithm;

import implementations.ConversionImp;
import implementations.algorithm.Algorithm;
import implementations.algorithm.AlgorithmNode;
import implementations.input.InputImp;
import interfaces.Conversion;
import interfaces.Input;
import org.junit.Test;

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
}
