import implementations.ConversionImp;
import implementations.algorithm.Algorithm;
import implementations.io.InputImp;
import interfaces.Conversion;
import interfaces.DAG;
import interfaces.Input;

public class Main {
	public static void Main(String args[]) {
		Input input = new InputImp(args[0], args[1]);
		Conversion conversion = new ConversionImp(input);

		DAG dag = conversion.getDAG();

		Algorithm alg = new Algorithm(dag, input.getProcessorCount());
	}
}
