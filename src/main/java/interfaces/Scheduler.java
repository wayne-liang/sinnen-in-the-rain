package interfaces;

/**
 * This interface represents the concept of a scheduler.
 * (See scheduler implementation for more details)
 * 
 * @author Victor
 *
 */
public interface Scheduler {
	
	public int getBestStartTime();
	
	public int getBestProcessor();
	
	public int getCurrentStartTime();
	
	public int getCurrentProcessor();
	
	public void setCurrentAsBest();
	
	public void setCurrentStartTimeAndProcessor (int startTime, int processor);
	
}
