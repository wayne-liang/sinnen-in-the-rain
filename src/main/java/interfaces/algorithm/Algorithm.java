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
    HashMap<String, NodeSchedule> getCurrentBestSchedule();

    int getBestTotalTime();
}
