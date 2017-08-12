package interfaces.algorithm;

import interfaces.structures.NodeSchedule;

import java.util.HashMap;

public interface Algorithm {
    HashMap<String, NodeSchedule> getCurrentBestSchedule();

    int getBestTotalTime();
}
