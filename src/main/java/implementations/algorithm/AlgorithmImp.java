package implementations.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import implementations.structures.DAGImp;
import implementations.structures.NodeScheduleImp;
import implementations.structures.ScheduleImp;
import interfaces.algorithm.Algorithm;
import interfaces.algorithm.AlgorithmNode;
import interfaces.structures.DAG;
import interfaces.structures.Node;
import interfaces.structures.NodeSchedule;
import interfaces.structures.Schedule;
import visualisation.BarChartModel;
import visualisation.Clock;
import visualisation.ComboView;
import visualisation.TableModel;

/**
 * This class represents the algorithm to solve the scheduling problem.
 * The class is responsible for all DFS searches and maintaining a current best result.
 * The class also acts as a controller for the View to update the visualisation.
 * 
 * @author Daniel, Victor, Wayne
 */
public class AlgorithmImp implements Algorithm {
	private DAG _dag;
	private int _numberOfCores;
	private HashMap<String, NodeSchedule> _currentBestSchedule;
	private int _recursiveCalls = 0; //For benchmarking purposes only
	private TableModel _model;
	private int _bestTime = Integer.MAX_VALUE; 
	private BarChartModel _chartModel;
	private ComboView _schedule;

	public AlgorithmImp(int numberOfCores) {
		_dag = DAGImp.getInstance();
		_numberOfCores = numberOfCores;
		_currentBestSchedule = new HashMap<>();

		// TODO: Check if visualization is true, only then do we create the gui. 
		_model = TableModel.getInstance();
		_model.initModel(_currentBestSchedule, _dag, _numberOfCores);
		// Initialize BarChart Model:
		_chartModel = new BarChartModel();
		// set-up the GUI
		_schedule = new ComboView(_model,_dag, _numberOfCores,_chartModel);

		Schedule empty = new ScheduleImp(_numberOfCores);
		recursiveScheduleGeneration(new ArrayList<AlgorithmNode>(), AlgorithmNode.convertNodetoAlgorithmNode(_dag.getAllNodes()), empty);
		_model.changeData(_currentBestSchedule, _bestTime);
		// reset model once we're done with it.
		_model = TableModel.resetInstance();
		// STOP THE CLOCK, now that we're done with it and set Label to done.
		Clock.getInstance().stopClock();
		_schedule.setStatusLabel(Clock.getInstance().getProcessStatus());
	}

	/**
	 * This method recursively does the branch and bound traversal.
	 * It takes the list of processed, remaining and previous schedule and from there determines if we need to keep going
	 * or checking if it's better than the current time.
	 *
	 * Branch down by adding each node to all the cores and then branching. Check times against heuristics and best time
	 * to decide whether to bound.
	 *
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 */
	public int getRecursiveCalls() {
		return _recursiveCalls;
	}

	/**
	 * This method recursively generates all possible schedules given a list of nodes.
	 *
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 */
	private void recursiveScheduleGeneration(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, Schedule prev){
		_schedule.setCallsButtonText(_recursiveCalls++); //For debugging and for updating visualisation.

		//Base Case when there are no remaining nodes left to process
		if (remainingNodes.size() == 0) {
			Schedule finalSchedule = prev;
			if (finalSchedule.getTotalTime() < _bestTime) { //Found a new best schedule
				setNewBestSchedule(finalSchedule);
				_bestTime = finalSchedule.getTotalTime();

				// update view, now that a new schedule is available
				_chartModel.addDataToSeries(_bestTime);
				_model.changeData(_currentBestSchedule, _bestTime);
				_schedule.setBestTimeText(_bestTime);

			}
		} else {
			for (int i = 0; i < remainingNodes.size(); i++) {
				Schedule newSchedule;

				//Assign the node to each core and continue recursive call down the branch
				for (int j = 1; j <= _numberOfCores; j++) {
					//Create a clone of the next node and assign it to a core. Place that new core
					//on a copy of the processed list
					List<AlgorithmNode> newProcessed = new ArrayList<>(processed);
					AlgorithmNode node = remainingNodes.get(i).createClone();
					node.setCore(j);
					newProcessed.add(node);
					if (checkValidSchedule(newProcessed)) {
						newSchedule = prev.getNextSchedule(node);

						//If current >= best time, bound by moving to the next processor.
						if (newSchedule.getTotalTime() >= _bestTime) {
							continue;
						}
					} else { //Schedule is invalid, then pruning the subtree by moving to next node.
						break;
					}

					//Create a new remaining list and remove the node that has been added to the processed list
					List<AlgorithmNode> newRemaining = new ArrayList<>(remainingNodes);
					newRemaining.remove(i);

					recursiveScheduleGeneration(newProcessed, newRemaining, newSchedule);

					/*
					 * Pruning:
					 * 
					 * Heuristic #1 - Symmetry. (a1... would have a symmetry with 
					 * a2 ...)
					 * Also, (a1 b2 ...) would have a symmetry with (a1 b3...)
					 * 
					 * Heuristic #2 - Partial symmetry. (a1 b1 c2) would be the same as
					 * (a1 b1 c3), in which case this is a partial symmetry on subtree. 
					 * 
					 * Implementation logic: we can break if the current node's core has
					 * never appeared before. -> This will implement both heuristic #1 & #2
					 */
					List<Integer> coresAssigned = processed.stream()
							.map(AlgorithmNode::getCore)
							.collect(Collectors.toList());

					if (!coresAssigned.contains(node.getCore())) {
						break; 
					}
				}
			}
		}
	}

	private void setNewBestSchedule(Schedule finalSchedule) {
		for (int i = 0; i < finalSchedule.getSizeOfSchedule(); i++) {
			NodeSchedule nodeSchedule = new NodeScheduleImp(finalSchedule.getNodeStartTime(i), finalSchedule.getNodeCore(i));
			_currentBestSchedule.put(finalSchedule.getNodeName(i), nodeSchedule);
		}
	}

	/**
	 * This method determines whether a schedule is valid. It does this by ensuring a nodes predecessors are scheduled
	 * before the current node
	 *
	 * @param schedule
	 * @return true if the schedule is valid, false if not
	 */
	private boolean checkValidSchedule(List<AlgorithmNode> schedule) {
		if (schedule == null) {
			return false;
		}

		//Get the last node's predecessors
		Node currentNode = _dag.getNodeByName(schedule.get(schedule.size()-1).getNodeName());
		List<Node> predecessors = currentNode.getPredecessors();

		//If there are no predecessors, then it is a starting node.
		if (predecessors.size() == 0) {
			return true;
		} else if (schedule.size() == 1) { //if has predecessor, but is the only node, then invalid. 
			return false;
		}

		//Loop through the previous nodes in the schedule and count when a predecessor is found
		int counter = 0;
		for (int i = schedule.size() - 2; i >= 0; i--) {
			for (Node preNode : predecessors) {
				if (schedule.get(i).getNodeName().equals(preNode.getName())) {
					counter++;
					break;
				}
			}
		}

		//Check if all the predecessors were found
		if (counter != predecessors.size()) {
			return false;
		}
		return true;
	}

	/**
	 * Calculates the time cost of executing the given schedule, returning a complete ScheduleImp object.
	 * @param algNodes - A {@code List<AlgorithmNode>} given in the order of execution
	 * @return - ScheduleImp object with cost and execution time information
	 */
	@Deprecated
	private ScheduleImp calculateTotalTime(List<AlgorithmNode> algNodes) {
		//creating a corresponding array of Nodes
		List<Node> nodes = new ArrayList<>();

		//populating new nodes array with corresponding Node objects
		for (AlgorithmNode algNode : algNodes) {
			nodes.add(_dag.getNodeByName(algNode.getNodeName()));
		}

		//creating ArrayLists to represent the schedule for each core
		//NOTE: could change the coreSchedule to just an ArrayList that holds the most recently scheduled node for each core
		List<AlgorithmNode> latestAlgNodeInSchedules = Arrays.asList(new AlgorithmNode[_numberOfCores]);

		//creating a ScheduleImp object for holding the schedule start times for each node
		ScheduleImp st = new ScheduleImp(algNodes, _numberOfCores);

		//looping through all the AlgorithmNodes to set startTimes
		for (AlgorithmNode currentAlgNode : algNodes) {
			Node currentNode = nodes.get(algNodes.indexOf(currentAlgNode));
			int highestCost = 0;

			//calculate the highest time delay caused by dependencies
			try {
				highestCost = currentNode.getPredecessors().stream().map(node -> {
					int cost = st.getNodeStartTime(getIndexOfList(node, algNodes)) + node.getWeight();
					if (!(algNodes.get(getIndexOfList(node, algNodes)).getCore() == currentAlgNode.getCore())) {
						//add on arc weight, since they're on different cores
						cost += currentNode.getInArc(node).getWeight();
					}

					return cost;
				}).max(Comparator.naturalOrder()).get();
			} catch (NoSuchElementException e) {
				//Means there are no predecessors, highestCost stays 0
			}

			//calculate the time delay caused by previous processes on the same core
			AlgorithmNode latestNode = latestAlgNodeInSchedules.get(currentAlgNode.getCore() - 1);
			if (latestNode != null) {
				Node previousNode = _dag.getNodeByName(latestNode.getNodeName());
				int cost = previousNode.getWeight() + st.getNodeStartTime(algNodes.indexOf(latestNode));
				if (cost > highestCost) {
					highestCost = cost;
				}
			}

			//set currentAlgNode as the newest node to be scheduled on it's core
			latestAlgNodeInSchedules.set(currentAlgNode.getCore() - 1, currentAlgNode);

			//set ScheduleImp startTime for this node
			st.setStartTimeForNode(highestCost, algNodes.indexOf(currentAlgNode));
		}

		setTimeForSchedule(latestAlgNodeInSchedules, algNodes, st);

		return st;
	}

	/**
	 * Calculates and sets the total time in the {@code ScheduleImp} object given.
	 * Main purpose is to make the code more readable.
	 * @param latestAlgNodeInSchedules - {@code List<AlgorithmNode>} containing the last node in each processor
	 * @param algNodes - the same {@code List<AlgorithmnNode>} used to construct the {@code ScheduleImp} object
	 * @param st - {@code ScheduleImp} object to set the total time of
	 */
	@Deprecated
	private void setTimeForSchedule(List<AlgorithmNode> latestAlgNodeInSchedules, List<AlgorithmNode> algNodes, ScheduleImp st) {
		int totalTime = 0;
		for (int i = 1; i <= _numberOfCores; i++) {
			AlgorithmNode latestAlgNode = latestAlgNodeInSchedules.get(i - 1);

			int timeTaken = 0;
			if (latestAlgNode != null) {
				timeTaken = st.getNodeStartTime(algNodes.indexOf(latestAlgNode)) + _dag.getNodeByName(latestAlgNode.getNodeName()).getWeight();
			}

			if (timeTaken > totalTime) {
				totalTime = timeTaken;
			}
		}

		st.setTotalTime(totalTime);
	}

	/**
	 * Finds and returns the index position of the corresponding {@code AlgorithmNode} within the given {@code List<AlgorithmNode}
	 * @param node - {@code Node} to find the corresponding index position for
	 * @param algNodes - {@code List<AlgorithmNode>} to find the index for
	 * @return the index position of the corresponding {@code AlgorithmNode} object
	 */
	@Deprecated
	private int getIndexOfList(Node node, List<AlgorithmNode> algNodes) {
		AlgorithmNode correspondingNode = algNodes.stream().filter(n -> node.getName().equals(n.getNodeName()))
				.findFirst()
				.get();

		return algNodes.indexOf(correspondingNode);
	}

	@Override
	public HashMap<String, NodeSchedule> getCurrentBestSchedule() {
		return _currentBestSchedule;
	}

	@Override
	public int getBestTotalTime() {
		return _bestTime;
	}

	/**
	 * The wrapper methods purely for testing. (as the methods were declared to be private)
	 * @param algNodes
	 * @return
	 */
	@Deprecated
	public ScheduleImp calculateTotalTimeWrapper(List<AlgorithmNode> algNodes) {
		return calculateTotalTime(algNodes);
	}

	public boolean checkValidScheduleWrapper(List<AlgorithmNode> s1) {
		return checkValidSchedule(s1);
	}
}
