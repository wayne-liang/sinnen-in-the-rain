import implementations.ConversionImp;
import implementations.algorithm.AlgorithmImp;
import implementations.io.InputImp;
import interfaces.Conversion;
import interfaces.algorithm.Algorithm;
import interfaces.io.Input;
import interfaces.structures.DAG;

public class Main {
	public static void Main(String args[]) {
		Input input = new InputImp(args[0], args[1]);
		Conversion conversion = new ConversionImp(input);

		DAG dag = conversion.getDAG();

		Algorithm alg = new AlgorithmImp(dag, input.getProcessorCount());
	}
}
