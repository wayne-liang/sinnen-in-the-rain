package io;

import implementations.algorithm.AlgorithmImp;
import implementations.io.Conversion;
import implementations.io.InputImp;
import implementations.io.OutputImp;
import implementations.structures.DAGImp;
import interfaces.io.Input;
import interfaces.structures.DAG;
import org.junit.Before;
import org.junit.Test;

/**
 * This class tests the OutputImp (by printing to console)
 * @author Victor
 *
 */
public class TestOutputImp {
	public static final String FILENAME = "test.dot";

	@Before
	public void before() {
		DAGImp.getNewInstance();
	}
	
    /**
     * Correct process count
     */
	@Test
    public void testProcessorNumber() {
		Input input = new InputImp(FILENAME, "2");
		Conversion conversion = new Conversion(input);

		AlgorithmImp alg = new AlgorithmImp(input.getProcessorCount(),true,1);
		OutputImp outputImp = new OutputImp(alg.getCurrentBestSchedule(), FILENAME);
		outputImp.printOutput();
	}


}
