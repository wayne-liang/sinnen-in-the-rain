package interfaces.algorithm;

import interfaces.structures.NodeSchedule;

import java.util.HashMap;

/**
 * The class which implements this interface represents the 
 * algorithm to solve the scheduling problem.
 * 
 * @see implementations.algorithm.AlgorithmImp
 *
 */
public interface Algorithm {
    /**
     * Get the current best schedule that we have found.
     *
     * @return hashmap representing each node and the corresponding schedule information
     */
    HashMap<String, NodeSchedule> getCurrentBestSchedule();

    /**
     * Get the time for the best schedule found.
     * @return int - representing the best time found
     */
    int getBestTotalTime();
}
