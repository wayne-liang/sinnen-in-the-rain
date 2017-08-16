import implementations.algorithm.AlgorithmImp;
import implementations.io.ConversionImp;
import implementations.io.InputImp;
import implementations.io.OutputImp;
import interfaces.Conversion;
import interfaces.algorithm.Algorithm;
import interfaces.io.Input;
import interfaces.structures.DAG;

public class Main {
	public static void main(String args[]) {
		Input input = new InputImp(args[0], args[1]);
		Conversion conversion = new ConversionImp(input);

		DAG dag = conversion.getDAG();

		Algorithm alg = new AlgorithmImp(dag, input.getProcessorCount());
		OutputImp outputImp = new OutputImp(alg.getCurrentBestSchedule(), args[0]);
		outputImp.outputToFile();
	}
}
