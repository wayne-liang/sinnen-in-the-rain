package implementations;

import interfaces.NodeSchedule;

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
	private int _bestStartTime = Integer.MAX_VALUE;
	private int _bestCore = Integer.MAX_VALUE;

	@Override
	public int getBestStartTime() {
		return _bestStartTime;
	}

	@Override
	public int getBestProcessor() {
		return _bestCore;
	}

	/**
	 * This method is called when a new best schedule is found
	 */
	@Override
	public void setBestTimes(int startTime, int core) {
		_bestStartTime = startTime;
		_bestCore = core;
	}
}
