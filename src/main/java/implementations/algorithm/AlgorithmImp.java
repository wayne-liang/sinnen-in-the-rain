package implementations.algorithm;//####[1]####
//####[1]####
import java.util.ArrayList;//####[3]####
import java.util.Arrays;//####[4]####
import java.util.Collections;//####[5]####
import java.util.Comparator;//####[6]####
import java.util.HashMap;//####[7]####
import java.util.HashSet;//####[8]####
import java.util.List;//####[9]####
import java.util.NoSuchElementException;//####[10]####
import java.util.Set;//####[11]####
import java.util.concurrent.Semaphore;//####[13]####
import java.lang.InterruptedException;//####[14]####
import implementations.structures.DAGImp;//####[16]####
import implementations.structures.NodeScheduleImp;//####[17]####
import implementations.structures.ScheduleImp;//####[18]####
import interfaces.algorithm.Algorithm;//####[19]####
import interfaces.algorithm.AlgorithmNode;//####[20]####
import interfaces.structures.DAG;//####[21]####
import interfaces.structures.Node;//####[22]####
import interfaces.structures.NodeSchedule;//####[23]####
import interfaces.structures.Schedule;//####[24]####
import visualisation.BarChartModel;//####[25]####
import visualisation.Clock;//####[26]####
import visualisation.ComboView;//####[27]####
import visualisation.TableModel;//####[28]####
//####[28]####
//-- ParaTask related imports//####[28]####
import pt.runtime.*;//####[28]####
import java.util.concurrent.ExecutionException;//####[28]####
import java.util.concurrent.locks.*;//####[28]####
import java.lang.reflect.*;//####[28]####
import pt.runtime.GuiThread;//####[28]####
import java.util.concurrent.BlockingQueue;//####[28]####
import java.util.ArrayList;//####[28]####
import java.util.List;//####[28]####
//####[28]####
/**
 * This class represents the algorithm to solve the scheduling problem.
 * The class is responsible for all DFS searches and maintaining a current best result.
 * The class also acts as a controller for the View to update the visualisation.
 * 
 * Algorithm @author: Daniel, Victor, Wayne
 * 
 * Visualisation @author: Pulkit
 *///####[38]####
public class AlgorithmImp implements Algorithm {//####[39]####
    static{ParaTask.init();}//####[39]####
    /*  ParaTask helper method to access private/protected slots *///####[39]####
    public void __pt__accessPrivateSlot(Method m, Object instance, TaskID arg, Object interResult ) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {//####[39]####
        if (m.getParameterTypes().length == 0)//####[39]####
            m.invoke(instance);//####[39]####
        else if ((m.getParameterTypes().length == 1))//####[39]####
            m.invoke(instance, arg);//####[39]####
        else //####[39]####
            m.invoke(instance, arg, interResult);//####[39]####
    }//####[39]####
//####[40]####
    private DAG _dag;//####[40]####
//####[41]####
    private int _numberOfCores;//####[41]####
//####[42]####
    private HashMap<String, NodeSchedule> _currentBestSchedule;//####[42]####
//####[43]####
    private int _recursiveCalls = 0;//####[43]####
//####[44]####
    private Semaphore _threads;//####[44]####
//####[45]####
    private int _numberOfThreads = 0;//####[45]####
//####[47]####
    private TableModel _model;//####[47]####
//####[48]####
    private BarChartModel _chartModel;//####[48]####
//####[49]####
    private ComboView _schedule;//####[49]####
//####[51]####
    private int _bestTime = Integer.MAX_VALUE;//####[51]####
//####[53]####
    private Set<Set<AlgorithmNode>> _uniqueProcessed;//####[53]####
//####[55]####
    private boolean _visualisation;//####[55]####
//####[58]####
    public AlgorithmImp(int numberOfCores, boolean visualisation, int noOfParallerCores) {//####[58]####
        _dag = DAGImp.getInstance();//####[59]####
        _numberOfCores = numberOfCores;//####[60]####
        _currentBestSchedule = new HashMap<String, NodeSchedule>();//####[61]####
        _visualisation = visualisation;//####[62]####
        _numberOfThreads = noOfParallerCores - 1;//####[63]####
        _threads = new Semaphore(_numberOfThreads);//####[64]####
        if (_visualisation) //####[66]####
        {//####[66]####
            _model = TableModel.getInstance();//####[67]####
            _model.initModel(_currentBestSchedule, _dag, _numberOfCores);//####[68]####
            _chartModel = new BarChartModel();//####[70]####
            _schedule = new ComboView(_model, _dag, _numberOfCores, _chartModel);//####[72]####
            _schedule.setParallelLabel(noOfParallerCores);//####[73]####
        }//####[74]####
        _uniqueProcessed = Collections.synchronizedSet(new HashSet<Set<AlgorithmNode>>());//####[77]####
        produceSequentialSchedule();//####[79]####
        produceGreedySchedule();//####[80]####
        Schedule emptySchedule = new ScheduleImp(_numberOfCores);//####[82]####
        recursiveScheduleGeneration(new ArrayList<AlgorithmNode>(), AlgorithmNode.convertNodetoAlgorithmNode(_dag.getAllNodes()), AlgorithmNode.convertNodetoAlgorithmNode(_dag.getStartNodes()), emptySchedule);//####[83]####
        try {//####[85]####
            _threads.acquire(_numberOfThreads);//####[86]####
        } catch (InterruptedException ex) {//####[87]####
            ex.printStackTrace();//####[88]####
        }//####[89]####
        if (_visualisation) //####[92]####
        {//####[92]####
            _model.changeData(_currentBestSchedule, _bestTime);//####[94]####
            _model = TableModel.resetInstance();//####[96]####
            Clock.getInstance().stopClock();//####[98]####
            _schedule.setStatusLabel(Clock.getInstance().getProcessStatus());//####[99]####
        }//####[100]####
    }//####[101]####
//####[106]####
    /**
	 * helper method for firing update.
	 *///####[106]####
    private void fireUpdateToGUI(int bestTime) {//####[106]####
        _chartModel.addDataToSeries(bestTime);//####[108]####
        _model.changeData(_currentBestSchedule, bestTime);//####[109]####
        _schedule.setBestTimeText(bestTime);//####[110]####
    }//####[111]####
//####[119]####
    /**
	 * This method will produce a sequential schedule to set the lower bound.
	 * 
	 * This will be used together will the greedy schedule to bound
	 * the DFS.
	 *///####[119]####
    private void produceSequentialSchedule() {//####[119]####
        List<Node> reachableNodes = new ArrayList<Node>();//####[120]####
        List<Node> completedNodes = new ArrayList<Node>();//####[121]####
        List<Node> remainingNodes = new ArrayList<Node>();//####[122]####
        reachableNodes.addAll(_dag.getStartNodes());//####[124]####
        remainingNodes.addAll(_dag.getAllNodes());//####[125]####
        Schedule schedule = new ScheduleImp(_numberOfCores);//####[127]####
        while (!reachableNodes.isEmpty()) //####[129]####
        {//####[129]####
            Node toBeScheduled = reachableNodes.get(0);//####[130]####
            AlgorithmNode algNode = new AlgorithmNodeImp(toBeScheduled.getName());//####[132]####
            algNode.setCore(1);//####[133]####
            schedule = schedule.getNextSchedule(algNode);//####[134]####
            completedNodes.add(toBeScheduled);//####[137]####
            reachableNodes.remove(toBeScheduled);//####[138]####
            remainingNodes.remove(toBeScheduled);//####[139]####
            for (Node rn : remainingNodes) //####[140]####
            {//####[140]####
                if (completedNodes.containsAll(rn.getPredecessors()) && !reachableNodes.contains(rn)) //####[141]####
                {//####[141]####
                    reachableNodes.add(rn);//####[142]####
                }//####[143]####
            }//####[144]####
        }//####[145]####
        setNewBestSchedule(schedule);//####[148]####
        _bestTime = schedule.getTotalTime();//####[149]####
    }//####[150]####
//####[158]####
    /**
	 * This method will produce a greedy schedule to set the lower bound.
	 * 
	 * This will be used together will the sequential schedule to bound
	 * the DFS.
	 *///####[158]####
    private void produceGreedySchedule() {//####[158]####
        List<Node> reachableNodes = new ArrayList<Node>();//####[159]####
        List<Node> completedNodes = new ArrayList<Node>();//####[160]####
        List<Node> remainingNodes = new ArrayList<Node>();//####[161]####
        reachableNodes.addAll(_dag.getStartNodes());//####[163]####
        remainingNodes.addAll(_dag.getAllNodes());//####[164]####
        Schedule schedule = new ScheduleImp(_numberOfCores);//####[166]####
        while (!reachableNodes.isEmpty()) //####[168]####
        {//####[168]####
            List<Integer> reachableAmount = new ArrayList<Integer>();//####[170]####
            for (Node n : reachableNodes) //####[171]####
            {//####[171]####
                reachableAmount.add(n.getSuccessors().size());//####[172]####
            }//####[173]####
            int maxIndex = reachableAmount.indexOf(Collections.max(reachableAmount));//####[174]####
            Node toBeScheduled = reachableNodes.get(maxIndex);//####[175]####
            List<Integer> earliestStartTimes = new ArrayList<Integer>();//####[178]####
            for (int i = 1; i <= _numberOfCores; i++) //####[179]####
            {//####[179]####
                int coreStart = schedule.getFinishTimeForCore(i);//####[180]####
                AlgorithmNode algNode = new AlgorithmNodeImp(toBeScheduled.getName());//####[181]####
                algNode.setCore(i);//####[182]####
                int depStart = schedule.getDependencyBasedStartTime(toBeScheduled, algNode);//####[183]####
                earliestStartTimes.add((coreStart > depStart) ? coreStart : depStart);//####[184]####
            }//####[185]####
            int earliestCoreNo = earliestStartTimes.indexOf(Collections.min(earliestStartTimes)) + 1;//####[186]####
            AlgorithmNode algNode = new AlgorithmNodeImp(toBeScheduled.getName());//####[188]####
            algNode.setCore(earliestCoreNo);//####[189]####
            schedule = schedule.getNextSchedule(algNode);//####[190]####
            completedNodes.add(toBeScheduled);//####[193]####
            reachableNodes.remove(toBeScheduled);//####[194]####
            remainingNodes.remove(toBeScheduled);//####[195]####
            for (Node rn : remainingNodes) //####[196]####
            {//####[196]####
                if (completedNodes.containsAll(rn.getPredecessors()) && !reachableNodes.contains(rn)) //####[197]####
                {//####[197]####
                    reachableNodes.add(rn);//####[198]####
                }//####[199]####
            }//####[200]####
        }//####[201]####
        if (schedule.getTotalTime() < _bestTime) //####[203]####
        {//####[203]####
            setNewBestSchedule(schedule);//####[204]####
            _bestTime = schedule.getTotalTime();//####[205]####
        }//####[206]####
    }//####[207]####
//####[214]####
    /**
	 * Purely for benchmarking purposes
	 *
	 * @return number of times the recursive method was called
	 *///####[214]####
    public int getRecursiveCalls() {//####[214]####
        return _recursiveCalls;//####[215]####
    }//####[216]####
//####[224]####
    /**
	 * This method is a thread-safe way of comparing a schedule against the current 
	 * best schedule so far, replacing it if the new one is better.
	 * 
	 * @param newSchedule 	- The new Schedule to compare 
	 *///####[224]####
    private synchronized void compareSchedules(Schedule newSchedule) {//####[224]####
        if (newSchedule.getTotalTime() < _bestTime) //####[225]####
        {//####[225]####
            setNewBestSchedule(newSchedule);//####[226]####
            _bestTime = newSchedule.getTotalTime();//####[227]####
        }//####[228]####
    }//####[229]####
//####[241]####
    private static volatile Method __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_ListAlgorithmNode_Schedule_method = null;//####[241]####
    private synchronized static void __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet() {//####[241]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[241]####
            try {//####[241]####
                __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_ListAlgorithmNode_Schedule_method = ParaTaskHelper.getDeclaredMethod(new ParaTaskHelper.ClassGetter().getCurrentClass(), "__pt__recursiveScheduleGenerationTask", new Class[] {//####[241]####
                    List.class, List.class, List.class, Schedule.class//####[241]####
                });//####[241]####
            } catch (Exception e) {//####[241]####
                e.printStackTrace();//####[241]####
            }//####[241]####
        }//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed             - A list of processed nodes
	 * @param remainingNodes        - A list of nodes remaining to be processed
	 * @param quasiReachableNodes   - A list of nodes which are a superset of currently reachable nodes
	 * @param prev			        - The previous schedule.
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(Object processed, Object remainingNodes, Object quasiReachableNodes, Object prev) {//####[241]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[241]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, quasiReachableNodes, prev, new TaskInfo());//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed             - A list of processed nodes
	 * @param remainingNodes        - A list of nodes remaining to be processed
	 * @param quasiReachableNodes   - A list of nodes which are a superset of currently reachable nodes
	 * @param prev			        - The previous schedule.
	 *///####[241]####
    private TaskID<Void> recursiveScheduleGenerationTask(Object processed, Object remainingNodes, Object quasiReachableNodes, Object prev, TaskInfo taskinfo) {//####[241]####
        // ensure Method variable is set//####[241]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[241]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[241]####
        }//####[241]####
        List<Integer> __pt__taskIdIndexList = new ArrayList<Integer>();//####[241]####
        List<Integer> __pt__queueIndexList = new ArrayList<Integer>();//####[241]####
        if (processed instanceof BlockingQueue) {//####[241]####
            __pt__queueIndexList.add(0);//####[241]####
        }//####[241]####
        if (processed instanceof TaskID) {//####[241]####
            taskinfo.addDependsOn((TaskID)processed);//####[241]####
            __pt__taskIdIndexList.add(0);//####[241]####
        }//####[241]####
        if (remainingNodes instanceof BlockingQueue) {//####[241]####
            __pt__queueIndexList.add(1);//####[241]####
        }//####[241]####
        if (remainingNodes instanceof TaskID) {//####[241]####
            taskinfo.addDependsOn((TaskID)remainingNodes);//####[241]####
            __pt__taskIdIndexList.add(1);//####[241]####
        }//####[241]####
        if (quasiReachableNodes instanceof BlockingQueue) {//####[241]####
            __pt__queueIndexList.add(2);//####[241]####
        }//####[241]####
        if (quasiReachableNodes instanceof TaskID) {//####[241]####
            taskinfo.addDependsOn((TaskID)quasiReachableNodes);//####[241]####
            __pt__taskIdIndexList.add(2);//####[241]####
        }//####[241]####
        if (prev instanceof BlockingQueue) {//####[241]####
            __pt__queueIndexList.add(3);//####[241]####
        }//####[241]####
        if (prev instanceof TaskID) {//####[241]####
            taskinfo.addDependsOn((TaskID)prev);//####[241]####
            __pt__taskIdIndexList.add(3);//####[241]####
        }//####[241]####
        int[] __pt__queueIndexArray = new int[__pt__queueIndexList.size()];//####[241]####
        for (int __pt__i = 0; __pt__i < __pt__queueIndexArray.length; __pt__i++) {//####[241]####
            __pt__queueIndexArray[__pt__i] = __pt__queueIndexList.get(__pt__i);//####[241]####
        }//####[241]####
        taskinfo.setQueueArgIndexes(__pt__queueIndexArray);//####[241]####
        if (__pt__queueIndexArray.length > 0) {//####[241]####
            taskinfo.setIsPipeline(true);//####[241]####
        }//####[241]####
        int[] __pt__taskIdIndexArray = new int[__pt__taskIdIndexList.size()];//####[241]####
        for (int __pt__i = 0; __pt__i < __pt__taskIdIndexArray.length; __pt__i++) {//####[241]####
            __pt__taskIdIndexArray[__pt__i] = __pt__taskIdIndexList.get(__pt__i);//####[241]####
        }//####[241]####
        taskinfo.setTaskIdArgIndexes(__pt__taskIdIndexArray);//####[241]####
        taskinfo.setParameters(processed, remainingNodes, quasiReachableNodes, prev);//####[241]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[241]####
        taskinfo.setInstance(this);//####[241]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[241]####
    }//####[241]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed             - A list of processed nodes
	 * @param remainingNodes        - A list of nodes remaining to be processed
	 * @param quasiReachableNodes   - A list of nodes which are a superset of currently reachable nodes
	 * @param prev			        - The previous schedule.
	 *///####[241]####
    public void __pt__recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, List<AlgorithmNode> quasiReachableNodes, Schedule prev) {//####[241]####
        recursiveScheduleGeneration(processed, remainingNodes, quasiReachableNodes, prev);//####[242]####
        _threads.release();//####[243]####
    }//####[244]####
//####[244]####
//####[259]####
    /**
	 * This method recursively does the branch and bound traversal.
	 * It takes the lists of processed, remaining, and quasi-reachable nodes, along with the previous schedule.
	 * From there, it determines if we need to keep going by checking if it's better than the current time.
	 *
	 * Branch down by adding each node to all the cores and then branching. Check times against heuristics and best time
	 * to decide whether to bound.
	 *
	 * @param processed             - A list of processed nodes
	 * @param remainingNodes        - A list of nodes remaining to be processed
	 * @param quasiReachableNodes   - A list of nodes which are a superset of currently reachable nodes
	 * @param prev			        - The previous schedule.
	 *///####[259]####
    private void recursiveScheduleGeneration(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, List<AlgorithmNode> quasiReachableNodes, Schedule prev) {//####[259]####
        if (_visualisation) //####[260]####
        {//####[260]####
            _schedule.setCallsButtonText(_recursiveCalls++);//####[261]####
        }//####[262]####
        if (quasiReachableNodes.size() == 0) //####[265]####
        {//####[265]####
            Schedule finalSchedule = prev;//####[266]####
            compareSchedules(finalSchedule);//####[268]####
        } else {//####[269]####
            for (int i = 0; i < quasiReachableNodes.size(); i++) //####[270]####
            {//####[270]####
                Schedule newSchedule;//####[271]####
                for (int j = 1; j <= _numberOfCores; j++) //####[274]####
                {//####[274]####
                    List<AlgorithmNode> newProcessed = new ArrayList<AlgorithmNode>(processed);//####[278]####
                    AlgorithmNode node = quasiReachableNodes.get(i).createClone();//####[279]####
                    node.setCore(j);//####[280]####
                    newProcessed.add(node);//####[281]####
                    Set<AlgorithmNode> algNodesSet = new HashSet<AlgorithmNode>(newProcessed);//####[283]####
                    if (checkValidSchedule(newProcessed)) //####[285]####
                    {//####[285]####
                        int idleTime = prev.getTotalIdleTime();//####[294]####
                        double maxIdleTime = 0;//####[296]####
                        for (int k = 1; k <= _numberOfCores; k++) //####[297]####
                        {//####[297]####
                            int processorIdleTime = prev.getTotalTime() - prev.getFinishTimeForCore(i);//####[298]####
                            if (maxIdleTime < processorIdleTime) //####[299]####
                            {//####[299]####
                                maxIdleTime = processorIdleTime;//####[300]####
                            }//####[301]####
                        }//####[302]####
                        double maxNodeWeight = 0;//####[304]####
                        double remainingTime = 0;//####[305]####
                        for (AlgorithmNode algNode : remainingNodes) //####[306]####
                        {//####[306]####
                            Node coNode = _dag.getNodeByName(algNode.getNodeName());//####[307]####
                            if (maxNodeWeight < coNode.getWeight()) //####[308]####
                            {//####[308]####
                                maxNodeWeight = coNode.getWeight();//####[309]####
                            }//####[310]####
                            remainingTime += coNode.getWeight();//####[311]####
                        }//####[312]####
                        double shortestTimePossible = Math.max(Math.ceil((remainingTime - idleTime) / _numberOfCores), maxNodeWeight - maxIdleTime);//####[314]####
                        if (prev.getTotalTime() + shortestTimePossible >= _bestTime) //####[315]####
                        {//####[315]####
                            continue;//####[316]####
                        }//####[317]####
                        newSchedule = prev.getNextSchedule(node);//####[320]####
                        if ((newSchedule.getTotalTime() >= _bestTime)) //####[323]####
                        {//####[323]####
                            continue;//####[324]####
                        }//####[325]####
                    } else {//####[326]####
                        break;//####[327]####
                    }//####[328]####
                    if (_uniqueProcessed.contains(algNodesSet)) //####[339]####
                    {//####[339]####
                        continue;//####[340]####
                    } else {//####[342]####
                        _uniqueProcessed.add(algNodesSet);//####[343]####
                    }//####[344]####
                    List<AlgorithmNode> newRemaining = new ArrayList<AlgorithmNode>(remainingNodes);//####[348]####
                    newRemaining.remove(quasiReachableNodes.get(i));//####[349]####
                    List<AlgorithmNode> newReachable = new ArrayList<AlgorithmNode>(quasiReachableNodes);//####[351]####
                    newReachable.remove(i);//####[352]####
                    List<AlgorithmNode> toAdd = AlgorithmNode.convertNodetoAlgorithmNode(_dag.getNodeByName(node.getNodeName()).getSuccessors());//####[353]####
                    for (AlgorithmNode algNode : toAdd) //####[354]####
                    {//####[354]####
                        if (!newReachable.contains(algNode)) //####[355]####
                        {//####[355]####
                            newReachable.add(algNode);//####[356]####
                        }//####[357]####
                    }//####[358]####
                    List<Integer> coresAssigned = new ArrayList<Integer>();//####[373]####
                    for (AlgorithmNode algNode : processed) //####[374]####
                    {//####[374]####
                        coresAssigned.add(algNode.getCore());//####[375]####
                    }//####[376]####
                    if (!coresAssigned.contains(node.getCore())) //####[378]####
                    {//####[378]####
                        recursiveScheduleGeneration(newProcessed, newRemaining, newReachable, newSchedule);//####[380]####
                        break;//####[381]####
                    } else {//####[382]####
                        if (_dag.getNodeByName(node.getNodeName()).getSuccessors().size() > 1 && _threads.tryAcquire()) //####[387]####
                        {//####[387]####
                            recursiveScheduleGenerationTask(newProcessed, newRemaining, newReachable, newSchedule);//####[388]####
                        } else {//####[389]####
                            recursiveScheduleGeneration(newProcessed, newRemaining, newReachable, newSchedule);//####[390]####
                        }//####[391]####
                    }//####[392]####
                }//####[393]####
            }//####[394]####
        }//####[395]####
    }//####[396]####
//####[398]####
    private void setNewBestSchedule(Schedule finalSchedule) {//####[398]####
        for (int i = 0; i < finalSchedule.getSizeOfSchedule(); i++) //####[399]####
        {//####[399]####
            NodeSchedule nodeSchedule = new NodeScheduleImp(finalSchedule.getNodeStartTime(i), finalSchedule.getNodeCore(i));//####[400]####
            _currentBestSchedule.put(finalSchedule.getNodeName(i), nodeSchedule);//####[401]####
        }//####[402]####
        if (_visualisation) //####[404]####
        {//####[404]####
            fireUpdateToGUI(finalSchedule.getTotalTime());//####[405]####
        }//####[406]####
    }//####[407]####
//####[416]####
    /**
	 * This method determines whether a schedule is valid. It does this by ensuring a nodes predecessors are scheduled
	 * before the current node
	 *
	 * @param schedule
	 * @return true if the schedule is valid, false if not
	 *///####[416]####
    private boolean checkValidSchedule(List<AlgorithmNode> schedule) {//####[416]####
        if (schedule == null) //####[417]####
        {//####[417]####
            return false;//####[418]####
        }//####[419]####
        Node currentNode = _dag.getNodeByName(schedule.get(schedule.size() - 1).getNodeName());//####[422]####
        List<Node> predecessors = currentNode.getPredecessors();//####[423]####
        if (predecessors.size() == 0) //####[426]####
        {//####[426]####
            return true;//####[427]####
        } else if (schedule.size() == 1) //####[428]####
        {//####[428]####
            return false;//####[429]####
        }//####[430]####
        int counter = 0;//####[433]####
        for (int i = schedule.size() - 2; i >= 0; i--) //####[434]####
        {//####[434]####
            for (Node preNode : predecessors) //####[435]####
            {//####[435]####
                if (schedule.get(i).getNodeName().equals(preNode.getName())) //####[436]####
                {//####[436]####
                    counter++;//####[437]####
                    break;//####[438]####
                }//####[439]####
            }//####[440]####
        }//####[441]####
        if (counter != predecessors.size()) //####[444]####
        {//####[444]####
            return false;//####[445]####
        }//####[446]####
        return true;//####[447]####
    }//####[448]####
//####[451]####
    @Override//####[451]####
    public HashMap<String, NodeSchedule> getCurrentBestSchedule() {//####[451]####
        return _currentBestSchedule;//####[452]####
    }//####[453]####
//####[456]####
    @Override//####[456]####
    public int getBestTotalTime() {//####[456]####
        return _bestTime;//####[457]####
    }//####[458]####
//####[463]####
    /**
	 * The wrapper methods purely for testing. (as the methods were declared to be private)
	 *///####[463]####
    public boolean checkValidScheduleWrapper(List<AlgorithmNode> s1) {//####[463]####
        return checkValidSchedule(s1);//####[464]####
    }//####[465]####
}//####[465]####
