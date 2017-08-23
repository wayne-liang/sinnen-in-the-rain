package implementations.algorithm;

import implementations.structures.DAGImp;
import interfaces.structures.DAG;
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
	private AlgorithmNodeImp _currentNode;
	private DAG _dag;
	
	public ScheduleCalculator(Schedule prev, AlgorithmNodeImp current) {
		_prevSchedule = prev;
		_currentNode = current;
		_dag = DAGImp.getInstance();
	}
	
	
}
