package interfaces;

/**
 * This interface represents the concept of a scheduler.
 * (See scheduler implementation for more details)
 * 
 * @author Victor
 *
 */
public interface NodeSchedule {

	int getBestStartTime();

	int getBestProcessor();
}
