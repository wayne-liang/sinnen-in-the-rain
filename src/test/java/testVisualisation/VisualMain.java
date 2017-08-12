package testVisualisation;

import implementations.ConversionImp;
import implementations.algorithm.AlgorithmImp;
import implementations.io.InputImp;
import interfaces.Conversion;
import interfaces.algorithm.Algorithm;
import interfaces.io.Input;
import interfaces.structures.DAG;
import visualisation.GraphView;
import visualisation.GraphViewImp;
import visualisation.TableModel;

public class VisualMain {
	public static void main(String args[]) {
		Input input = new InputImp(args[0], args[1]);
		Conversion conversion = new ConversionImp(input);

		DAG dag = conversion.getDAG();

		AlgorithmImp alg = new AlgorithmImp(dag, input.getProcessorCount());
		TableModel _schedule = new TableModel(alg,dag,input.getProcessorCount());
		GraphView schedule = new GraphViewImp(_schedule);
	}
}
