package implementations;

import interfaces.Scheduler;

/**
 * This class represent a scheduler object. 
 * An scheduler object is always tied to a node.
 * 
 * It stores information on the current and best
 * start time and processor number.
 * @author Victor
 *
 */
public class SchedulerImp implements Scheduler {
	private int _bestStartTime = -1;
	private int _bestProcessor = -1;
	private int _currentStartTime = -1;
	private int _currentProcessor = -1;
	

	@Override
	public int getBestStartTime() {
		return _bestStartTime;
	}

	@Override
	public int getBestProcessor() {
		return _bestProcessor;
	}

	@Override
	public int getCurrentStartTime() {
		return _currentStartTime;
	}

	@Override
	public int getCurrentProcessor() {
		return _currentProcessor;
	}

	/**
	 * This method is called when a new best scheduler is found.
	 * It should set the best start time and best processor time
	 * to the current one. (Since the current is the best)
	 */
	@Override
	public void setCurrentAsBest() {
		_bestStartTime = _currentStartTime;
		_bestProcessor = _currentProcessor;
	}

	/**
	 * Use this method to set the current schedule
	 * @param startTime
	 * @param processor
	 */
	@Override
	public void setCurrentStartTimeAndProcessor(int startTime, int processor) {
		_currentStartTime = startTime;
		_currentProcessor = processor;
	}

}
