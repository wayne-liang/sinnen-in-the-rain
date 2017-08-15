package implementations.structures;

import interfaces.structures.NodeSchedule;

/**
 * This class represent a node scheduler object.
 * (That is, when a node should start to execute and on which core)
 * 
 * Whenever a new best schedule is found, objects of this class
 * will be created in the algorithm class, and stored into a hashmap 
 * of Node / NodeSchedule. 
 *
 * @author Victor
 */
public class NodeScheduleImp implements NodeSchedule {
	private final int _bestStartTime;
	private final int _bestCore;

	public NodeScheduleImp(int startTime, int core) {
		_bestStartTime = startTime;
		_bestCore = core;
	}

	@Override
	public int getBestStartTime() {
		return _bestStartTime;
	}

	@Override
	public int getBestProcessor() {
		return _bestCore;
	}
}
