package implementations.algorithm;

import interfaces.DAG;
import interfaces.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements the algorithim to solve the scheduling problem
 */
public class Algorithm {
	private DAG _dag;
	private int _numberOfCores;

	public Algorithm(DAG dag, int numberOfCores) {
		_dag = dag;
		_numberOfCores = numberOfCores;

		List<Node> allNodes = _dag.getAllNodes();
		recursiveScheduleGeneration(new ArrayList<>(), AlgorithmNode.convertNodetoAlgorithimNode(allNodes));
	}

	/**
	 * This method recursively generates all possible schedules given a list of nodes.
	 *
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 */
	private void recursiveScheduleGeneration(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes) {
		//Base Case
		if (remainingNodes.size() == 0) {
			for (AlgorithmNode node : processed) {
				System.out.printf(node.getNodeName() + node.getCore() + " ");
			}
			System.out.println();
		} else {
			for (int i = 0; i < remainingNodes.size(); i++) {
				for (int j = 1; j <= _numberOfCores; j++) {
					List<AlgorithmNode> newProcessed = new ArrayList<>(processed);
					AlgorithmNode node = remainingNodes.get(i);
					node.setCore(j);
					newProcessed.add(node);

					List<AlgorithmNode> newRemaining = new ArrayList<>(remainingNodes);
					newRemaining.remove(i);

					recursiveScheduleGeneration(newProcessed, newRemaining);
				}
			}
		}
	}
}
