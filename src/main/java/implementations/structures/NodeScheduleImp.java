package implementations.structures;

import interfaces.structures.NodeSchedule;

/**
 * This class represent a scheduler object.
 * An scheduler object is always tied to a node.
 * <p>
 * It stores information on the current and best
 * start time and processor number.
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
