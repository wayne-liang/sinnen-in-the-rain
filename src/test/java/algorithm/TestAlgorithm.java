package algorithm;

import implementations.ConversionImp;
import implementations.algorithm.Algorithm;
import implementations.input.InputImp;
import interfaces.Conversion;
import interfaces.Input;
import org.junit.Test;

public class TestAlgorithm {
	public static final String FILENAME = "test.dot";

	@Test
	public void testGenerateSchedule() {
		Input input = new InputImp(FILENAME, "2");
		Conversion conversion = new ConversionImp(input);
		Algorithm alg = new Algorithm(conversion.getDAG(), input.getProcessorCount());
	}
}
