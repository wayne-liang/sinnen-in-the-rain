package testOutput;

import org.junit.Test;

import implementations.ConversionImp;
import implementations.algorithm.Algorithm;
import implementations.io.InputImp;
import implementations.io.Output;
import interfaces.Conversion;
import interfaces.DAG;
import interfaces.Input;

/**
 * This class tests the Output (by printing to console)
 * @author Victor
 *
 */
public class TestOutput {
	public static final String FILENAME = "test.dot";
	
    /**
     * Correct process count
     */
	@Test
    public void testProcessorNumber() {
		Input input = new InputImp(FILENAME, "2");
		Conversion conversion = new ConversionImp(input);

		DAG dag = conversion.getDAG();

		Algorithm alg = new Algorithm(dag, input.getProcessorCount());
		Output output = new Output (alg.getCurrentBestSchedule(),FILENAME);
		output.printOutput();
    }


}
