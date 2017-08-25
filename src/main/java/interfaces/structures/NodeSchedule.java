package interfaces.structures;

/**
 * This interface represents the concept of a scheduler.
 * (See scheduler implementation for more details)
 * 
 * @see implementations.structures.NodeScheduleImp
 * 
 * @author Victor
 *
 */
public interface NodeSchedule {

	int getBestStartTime();

	int getBestProcessor();
}
