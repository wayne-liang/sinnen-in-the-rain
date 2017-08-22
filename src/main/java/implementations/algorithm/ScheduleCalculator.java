package implementations.algorithm;

import interfaces.algorithm.AlgorithmNode;
import interfaces.structures.DAG;
import interfaces.structures.Node;
import interfaces.structures.Schedule;

/**
 * This class represent the abstraction of a schedule calculator.
 * A schedule calculator's responsibility is to generate a schedule
 * (A schedule is an object that contains information about start
 * time and total time)
 * given a previous schedule and the new node that is added to it.
 * 
 * @author Victor
 *
 */
public class ScheduleCalculator {
	private Schedule _prevSchedule;
	private AlgorithmNode _currentNode;
	private DAG _dag;
	
	public ScheduleCalculator(Schedule prev, AlgorithmNode current, DAG dag) {
		_prevSchedule = prev;
		_currentNode = current;
		_dag = dag;
	}
	
	public Schedule generateSchedule() {
		return null;
	}
}
