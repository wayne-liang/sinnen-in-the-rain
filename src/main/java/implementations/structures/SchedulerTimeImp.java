package implementations.structures;

import implementations.algorithm.AlgorithmNodeImp;
import interfaces.structures.SchedulerTime;

import java.util.List;

/**
 * This class represents the time taken for a particular scheduler
 * to run.
 * 
 * It contains the starting time for each node,
 * as well as the total running time.
 * 
 * An object of this class should be returned when the time for
 * a scheduler (or a partial scheduler) is calculated.
 *  
 * @author Victor
 *
 */
public class SchedulerTimeImp implements SchedulerTime {
	private List<AlgorithmNodeImp> _algNodes;
	//The index for this field should match the index for the list of nodes.
	private int[] _startTimeForNode;// = new int[];
	private int _totalTime;

	public SchedulerTimeImp(List<AlgorithmNodeImp> algNodes) {
		_algNodes = algNodes;
		_startTimeForNode = new int[_algNodes.size()];
	}
	
	/**
	 * @param startTime
	 * @param index --- The index should match the index for the List<AlgorithmNodes>.
	 */
	@Override
	public void setStartTimeForNode (int startTime, int index) {
		_startTimeForNode[index] = startTime;
	}

	/**
	 * The use of this method may break encapsulation.
	 * Use the methods getNodeName(), getNodeStartTime(),
	 * getNodeCore() instead.
	 * @return
	 */
	@Deprecated
	public List<AlgorithmNodeImp> getAlgorithmNodes() {
		return _algNodes;
	}

	/**
	 * The getters below should be called
	 * when processing node time information.
	 *
	 * @return
	 */
	@Override
	public int getSizeOfScheduler() {
		return _algNodes.size();
	}
	
	/**
	 * The use of this method may break encapsulation. 
	 * Use the methods getNodeName(), getNodeStartTime(), 
	 * getNodeCore() instead. 
	 * @return
	 */
	@Deprecated
	public int[] getstartTimeForNodes() {
		return _startTimeForNode;
	}

	@Override
	public String getNodeName(int index) {
		return _algNodes.get(index).getNodeName();
	}

	@Override
	public int getNodeStartTime (int index) {
		return _startTimeForNode[index];
	}

	@Override
	public int getNodeCore (int index) {
		return _algNodes.get(index).getCore();
	}

	@Override
	public int getTotalTime() {
		return _totalTime;
	}

	@Override
	public void setTotalTime(int totalTime) {
		_totalTime = totalTime;
	}
}
