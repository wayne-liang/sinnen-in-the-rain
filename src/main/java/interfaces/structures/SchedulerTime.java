package interfaces.structures;

import implementations.algorithm.AlgorithmNodeImp;

import java.util.List;

public interface SchedulerTime {
    void setStartTimeForNode(int startTime, int index);

    @Deprecated
    List<AlgorithmNodeImp> getAlgorithmNodes();

    @Deprecated
    int[] getstartTimeForNodes();

    int getSizeOfScheduler();

    String getNodeName(int index);

    int getNodeStartTime(int index);

    int getNodeCore(int index);

    int getTotalTime();

    void setTotalTime(int totalTime);
}
