package implementations.algorithm;

import implementations.SchedulerTime;
import interfaces.DAG;
import interfaces.Node;

import java.util.*;

/**
 * This class implements the algorithm to solve the scheduling problem
 */
public class Algorithm {
	private DAG _dag;
	private int _numberOfCores;
	private List<List<AlgorithmNode>> _generatedSchedules; //This holds all the generated schedules

	public Algorithm(DAG dag, int numberOfCores) {
		_dag = dag;
		_numberOfCores = numberOfCores;
		_generatedSchedules = new ArrayList<>();

		recursiveScheduleGeneration(new ArrayList<>(), AlgorithmNode.convertNodetoAlgorithimNode(_dag.getAllNodes()));
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
			_generatedSchedules.add(processed);
		} else {
			for (int i = 0; i < remainingNodes.size(); i++) {
				for (int j = 0; j < _numberOfCores; j++) {
					List<AlgorithmNode> newProcessed = new ArrayList<>(processed);
					AlgorithmNode node = remainingNodes.get(i).createClone();
					node.setCore(j);
					newProcessed.add(node);

					List<AlgorithmNode> newRemaining = new ArrayList<>(remainingNodes);
					newRemaining.remove(i);

					recursiveScheduleGeneration(newProcessed, newRemaining);
				}
			}
		}
	}

	//For testing
	public List<List<AlgorithmNode>> getSchedules() {
		return _generatedSchedules;
	}

	/**
	 * Calculates the time cost of executing the given schedule, returning a complete SchedulerTime object.
	 * @param algNodes - A {@code List<AlgorithmNode>} given in the order of execution
	 * @return - SchedulerTime object with cost and execution time information
	 */
	public SchedulerTime getCost(List<AlgorithmNode> algNodes) {
		//making a copy that I can manipulate
		//this copy is going work as a list of nodes that still need their start time calculated
		List<AlgorithmNode> algNodesCopy = new ArrayList<>(algNodes);
		List<Node> nodes = new ArrayList<>();

		//populating new nodes array with corresponding Node objects
		for (AlgorithmNode algNode : algNodesCopy) {
			nodes.add(_dag.getNodeByName(algNode.getNodeName()));
		}

		//creating ArrayLists to represent the schedule for each core
		ArrayList<ArrayList<AlgorithmNode>> coreSchedules = new ArrayList<>();
		for (int i = 0; i < _numberOfCores; i++) {
			coreSchedules.add(new ArrayList<>());
		}

		//creating a SchedulerTime object for holding the schedule start times for each node
		SchedulerTime st = new SchedulerTime(algNodes);

		while (!algNodesCopy.isEmpty()) {
			for (AlgorithmNode algNode : algNodesCopy) {
				Node currentNode = nodes.get(algNodesCopy.indexOf(algNode));

				//check if dependencies have been scheduled
				

				//if the dependencies haven't been scheduled yet, move on and come back until they are
				if (dependenciesNotFinished) {

				} else {
					int highestCost = 0;
					int coreNum = algNode.getCore();
					ArrayList<AlgorithmNode> currentCore = coreSchedules.get(coreNum);
					currentCore.add(algNode);

					//check when previous process on given core is finished, if there is one
					if (currentCore.size() > 1) {
						int cost = _dag.getNodeByName(currentCore.get(currentCore.size() - 2).getNodeName()).getWeight();
						if (cost > highestCost) {
							highestCost = cost;
						}
					}

					//check cost of dependencies
				}
			}

			//have to remove the nodes which have successfully had their start times calculated
		}
	}
}
