package interfaces.structures;

import implementations.algorithm.AlgorithmNodeImp;

import java.util.List;

public interface Schedule {
    void setStartTimeForNode(int startTime, int index);

    @Deprecated
    List<AlgorithmNodeImp> getAlgorithmNodes();

    @Deprecated
    int[] getstartTimeForNodes();

    int getSizeOfSchedule();

    String getNodeName(int index);

    int getNodeStartTime(int index);

    int getNodeCore(int index);

    int getTotalTime();

    void setTotalTime(int totalTime);
}
