package io;

import implementations.ConversionImp;
import implementations.algorithm.AlgorithmImp;
import implementations.io.InputImp;
import implementations.io.OutputImp;
import interfaces.Conversion;
import interfaces.io.Input;
import interfaces.structures.DAG;
import org.junit.Test;

/**
 * This class tests the OutputImp (by printing to console)
 * @author Victor
 *
 */
public class TestOutputImp {
	public static final String FILENAME = "test.dot";
	
    /**
     * Correct process count
     */
	@Test
    public void testProcessorNumber() {
		Input input = new InputImp(FILENAME, "2");
		Conversion conversion = new ConversionImp(input);

		DAG dag = conversion.getDAG();

		AlgorithmImp alg = new AlgorithmImp(dag, input.getProcessorCount());
		OutputImp outputImp = new OutputImp(alg.getCurrentBestSchedule(), FILENAME);
		outputImp.printOutput();
	}


}
