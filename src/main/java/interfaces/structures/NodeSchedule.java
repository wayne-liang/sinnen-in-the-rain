package interfaces.structures;

/**
 * This interface represent a node scheduler object.
 * (That is, when a node should start to execute and on which core)
 * 
 * Whenever a new best schedule is found, objects of this class
 * will be created in the algorithm class, and stored into a hashmap 
 * of Node / NodeSchedule. 
 *
 * @author Victor
 */
public interface NodeSchedule {

	int getBestStartTime();

	int getBestProcessor();
}
