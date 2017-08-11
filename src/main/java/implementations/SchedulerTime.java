package implementations;

import implementations.algorithm.AlgorithmNode;

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
public class SchedulerTime {
	private List<AlgorithmNode> _algNodes;
	//The index for this field should match the index for the list of nodes.
	private int[] _startTimeForNode;// = new int[];
	private int _totalTime;
	
	public SchedulerTime (List<AlgorithmNode> algNodes) {
		_algNodes = algNodes;
		_startTimeForNode = new int[_algNodes.size()];
	}
	
	/**
	 * @param startTime
	 * @param index --- The index should match the index for the List<AlgorithmNodes>.
	 */
	public void setStartTimeForNode (int startTime, int index) {
		_startTimeForNode[index] = startTime;
	}
	
	public void setTotalTime (int totalTime) {
		_totalTime = totalTime;
	}
	
	/**
	 * The use of this method may break encapsulation. 
	 * Use the methods getNodeName(), getNodeStartTime(), 
	 * getNodeCore() instead. 
	 * @return
	 */
	@Deprecated
	public List<AlgorithmNode> getAlgorithmNodes() {
		return _algNodes;
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
	
	
	/**
	 * The getters below should be called 
	 * when processing node time information.
	 * @return
	 */
	public int getSizeOfScheduler () {
		return _algNodes.size();
	}
	
	public String getNodeName (int index) {
		return _algNodes.get(index).getNodeName();
	}
	
	public int getNodeStartTime (int index) {
		return _startTimeForNode[index];
	}
	
	public int getNodeCore (int index) {
		return _algNodes.get(index).getCore();
	}
	
	public int getTotalTime() {
		return _totalTime;
	}
}
