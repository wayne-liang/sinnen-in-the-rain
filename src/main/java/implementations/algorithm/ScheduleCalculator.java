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
	
	public Schedule generateSchedule() {
		Schedule newSchedule;
		if (_prevSchedule.getSizeOfSchedule() == 0) { //Empty scheule, this is the first node.
			newSchedule = _prevSchedule.appendNodeToSchedule(_currentNode, 0); //start on time 0 
		} else {
			AlgorithmNodeImp lastNodeOnCore = _prevSchedule.getLastNodeOnCore(_currentNode.getCore());
			
			int endTimeForCore;
			if (lastNodeOnCore == null) { 
				endTimeForCore = 0;
			} else {
				//TODO
			}
			newSchedule = null;
		}
		
		return newSchedule;
	}
}
