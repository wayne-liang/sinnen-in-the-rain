package implementations.structures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import interfaces.algorithm.AlgorithmNode;
import interfaces.structures.Schedule;

/**
 * This class represents the abstraction of a schedule (or a partial schedule)
 * A schedule contains an ordered list of nodes together with an assigned core. 
 * (This is known as AlgorithmNode).
 * 
 * Schedule also contains the starting time for each node,
 * as well as the total running time for the entire schedule. 
 * (These time are computed and set using setters.)
 * 
 * An object of this class should be returned when the time for
 * a schedule (or a partial schedule) is calculated.
 *  
 * @author Victor
 *
 */
public class ScheduleImp implements Schedule {
	private List<AlgorithmNode> _algNodes;
	//The index for this field should match the index for the list of nodes.
	private int[] _startTimeForNode;// = new int[];
	private int _totalTime;
	private int _numberOfCores;
	
	private Map<Integer, AlgorithmNode> _lastAlgNodeOnCore;

	/**
	 * The default constructor should only be called when 
	 * the schedule is empty. (No node is in the schedule).
	 */
	public ScheduleImp(int numberOfCores) {
		_algNodes = new ArrayList<AlgorithmNode>();
		_numberOfCores = numberOfCores;
		
		_lastAlgNodeOnCore = new HashMap<Integer, AlgorithmNode>();
		for (int i = 0; i < _numberOfCores; i++){
			_lastAlgNodeOnCore.put(i+1, null); //Empty schedule.
		}
	}
	
	public ScheduleImp(List<AlgorithmNode> algNodes, int numberOfCores) {
		_algNodes = algNodes;
		_startTimeForNode = new int[_algNodes.size()];
		_numberOfCores = numberOfCores;
		
		//Calculate last schedule on core.
		_lastAlgNodeOnCore = new HashMap<Integer, AlgorithmNode>();
		for (int i = 0; i < _numberOfCores; i++){
			_lastAlgNodeOnCore.put(i+1, null); //Empty schedule.
		}
		List<Integer> list = algNodes.stream().map(AlgorithmNode::getCore).collect(Collectors.toList());
		for (int i = 0; i < _numberOfCores; i++){
			int lastNodeIndex = list.lastIndexOf(i+1);//i+1 to match the core as cores starts from 1.
			if (lastNodeIndex != -1){
				_lastAlgNodeOnCore.put(lastNodeIndex, algNodes.get(lastNodeIndex));
			}
		}
	}
	
	/**
	 * private constructor for copying purpose only.
	 * 
	 * @param algNodes
	 * @param numberOfCores
	 * @param lastAlgNodeOnCore
	 */
	private ScheduleImp(List<AlgorithmNode> algNodes, int numberOfCores, Map<Integer, AlgorithmNode> lastAlgNodeOnCore){
		this(algNodes, numberOfCores);
		_lastAlgNodeOnCore = lastAlgNodeOnCore;
	}
	
	/**
	 * This class should clone the old Schedule, create a new Schedule object
	 * with the new node appended on the end. 
	 * 
	 * @param current
	 * @param startTime
	 * @return
	 */
	@Override
	public Schedule appendNodeToSchedule(AlgorithmNode current, int startTime) {
		int size = this.getSizeOfSchedule();
		
		List<AlgorithmNode> algNodes = _algNodes;
		int[] startTimeForNode = _startTimeForNode;
		algNodes.add(current);
		startTimeForNode[size] = startTime;
		
		return new ScheduleImp (_algNodes, _numberOfCores, _lastAlgNodeOnCore);
	}
	
	public Schedule generateSchedule(AlgorithmNode currentNode) {
		Schedule newSchedule;
		if (this.getSizeOfSchedule() == 0) { //Empty scheule, this is the first node.
			newSchedule = this.appendNodeToSchedule(currentNode, 0); //start on time 0 
		} else {
			AlgorithmNode lastNodeOnCore = this.getLastNodeOnCore(currentNode.getCore());
			
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
	public List<AlgorithmNode> getAlgorithmNodes() {
		return _algNodes;
	}

	/**
	 * The getters below should be called
	 * when processing node time information.
	 *
	 * @return
	 */
	@Override
	public int getSizeOfSchedule() {
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
	
	@Override
	public AlgorithmNode getLastNodeOnCore (int core){
		return _lastAlgNodeOnCore.get(core);
	}
}
