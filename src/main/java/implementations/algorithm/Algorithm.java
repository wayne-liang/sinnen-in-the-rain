package implementations.algorithm;

import interfaces.DAG;
import interfaces.Node;

public class Algorithm {
	public DAG _dag;

	public Algorithm(DAG dag) {
		_dag = dag;
		generateSchedule();
	}

	private void generateSchedule() {
		Node node = _dag.getAllNodes().get(0);
		System.out.println(node.getName());
	}
}
