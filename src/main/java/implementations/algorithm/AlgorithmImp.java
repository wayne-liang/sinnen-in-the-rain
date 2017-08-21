package implementations.algorithm;//####[1]####
//####[1]####
import implementations.structures.NodeScheduleImp;//####[3]####
import implementations.structures.ScheduleImp;//####[4]####
import interfaces.algorithm.Algorithm;//####[5]####
import interfaces.algorithm.AlgorithmNode;//####[6]####
import interfaces.structures.DAG;//####[7]####
import interfaces.structures.Node;//####[8]####
import interfaces.structures.NodeSchedule;//####[9]####
import java.util.*;//####[11]####
import java.util.stream.Collectors;//####[12]####
import java.util.concurrent.Semaphore;//####[13]####
import java.lang.InterruptedException;//####[14]####
//####[14]####
//-- ParaTask related imports//####[14]####
import pt.runtime.*;//####[14]####
import java.util.concurrent.ExecutionException;//####[14]####
import java.util.concurrent.locks.*;//####[14]####
import java.lang.reflect.*;//####[14]####
import pt.runtime.GuiThread;//####[14]####
import java.util.concurrent.BlockingQueue;//####[14]####
import java.util.ArrayList;//####[14]####
import java.util.List;//####[14]####
//####[14]####
/**
 * This class implements the algorithm to solve the scheduling problem
 *///####[18]####
public class AlgorithmImp implements Algorithm {//####[19]####
    static{ParaTask.init();}//####[19]####
    /*  ParaTask helper method to access private/protected slots *///####[19]####
    public void __pt__accessPrivateSlot(Method m, Object instance, TaskID arg, Object interResult ) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {//####[19]####
        if (m.getParameterTypes().length == 0)//####[19]####
            m.invoke(instance);//####[19]####
        else if ((m.getParameterTypes().length == 1))//####[19]####
            m.invoke(instance, arg);//####[19]####
        else //####[19]####
            m.invoke(instance, arg, interResult);//####[19]####
    }//####[19]####
//####[20]####
    private DAG _dag;//####[20]####
//####[21]####
    private int _numberOfCores;//####[21]####
//####[22]####
    private HashMap<String, NodeSchedule> _currentBestSchedule;//####[22]####
//####[23]####
    private int _recursiveCalls = 0;//####[23]####
//####[24]####
    private final Semaphore _threads;//####[24]####
//####[25]####
    private final int _numberOfThreads = 16;//####[25]####
//####[27]####
    private int _bestTime = Integer.MAX_VALUE;//####[27]####
//####[29]####
    public AlgorithmImp(DAG dag, int numberOfCores) {//####[29]####
        _dag = dag;//####[30]####
        _numberOfCores = numberOfCores;//####[31]####
        _currentBestSchedule = new HashMap<String, NodeSchedule>();//####[32]####
        _threads = new Semaphore(_numberOfThreads);//####[34]####
        recursiveScheduleGeneration(new ArrayList(), AlgorithmNode.convertNodetoAlgorithmNode(_dag.getAllNodes()));//####[36]####
        try {//####[39]####
            _threads.acquire(_numberOfThreads);//####[40]####
        } catch (InterruptedException ex) {//####[41]####
            ex.printStackTrace();//####[42]####
        }//####[43]####
    }//####[44]####
//####[51]####
    /**
	 * Purely for benchmarking purposes
	 *
	 * @return number of times the recursive method was called
	 *///####[51]####
    public int getRecursiveCalls() {//####[51]####
        return _recursiveCalls;//####[52]####
    }//####[53]####
//####[61]####
    /**
	 * This method recursively generates all possible schedules given a list of nodes.
	 *
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 *///####[61]####
    private void recursiveScheduleGeneration(List<AlgorithmNodeImp> processed, List<AlgorithmNodeImp> remainingNodes) {//####[61]####
        _recursiveCalls++;//####[62]####
        if (remainingNodes.size() == 0) //####[65]####
        {//####[65]####
            ScheduleImp st = calculateTotalTime(processed);//####[66]####
            compareSchedules(st);//####[67]####
        } else {//####[68]####
            for (int i = 0; i < remainingNodes.size(); i++) //####[69]####
            {//####[69]####
                for (int j = 1; j <= _numberOfCores; j++) //####[70]####
                {//####[70]####
                    List<AlgorithmNodeImp> newProcessed = new ArrayList<AlgorithmNodeImp>(processed);//####[71]####
                    AlgorithmNodeImp node = remainingNodes.get(i).createClone();//####[72]####
                    node.setCore(j);//####[73]####
                    newProcessed.add(node);//####[74]####
                    if (checkValidSchedule(newProcessed)) //####[76]####
                    {//####[76]####
                        ScheduleImp st = calculateTotalTime(newProcessed);//####[77]####
                        if (st.getTotalTime() >= _bestTime) //####[80]####
                        {//####[80]####
                            continue;//####[81]####
                        }//####[82]####
                    } else {//####[83]####
                        break;//####[84]####
                    }//####[85]####
                    List<AlgorithmNodeImp> newRemaining = new ArrayList<AlgorithmNodeImp>(remainingNodes);//####[93]####
                    newRemaining.remove(i);//####[94]####
                    if (_dag.getNodeByName(node.getNodeName()).getSuccessors().size() > 1 && _threads.tryAcquire()) //####[96]####
                    {//####[96]####
                        recursiveScheduleGenerationTask(newProcessed, newRemaining);//####[97]####
                    } else {//####[98]####
                        recursiveScheduleGeneration(newProcessed, newRemaining);//####[99]####
                    }//####[100]####
                    List<Integer> coresAssigned = new ArrayList<Integer>();//####[110]####
                    for (AlgorithmNodeImp algNode : newProcessed) //####[111]####
                    {//####[111]####
                        if (!coresAssigned.contains(algNode.getCore())) //####[112]####
                        {//####[112]####
                            coresAssigned.add(algNode.getCore());//####[113]####
                        } else {//####[114]####
                            break;//####[116]####
                        }//####[117]####
                    }//####[118]####
                    if (coresAssigned.size() == newProcessed.size()) //####[120]####
                    {//####[120]####
                        break;//####[121]####
                    }//####[122]####
                }//####[123]####
            }//####[124]####
        }//####[125]####
    }//####[126]####
//####[128]####
    private synchronized void compareSchedules(ScheduleImp st) {//####[128]####
        if (st.getTotalTime() < _bestTime) //####[129]####
        {//####[129]####
            setNewBestSchedule(st);//####[130]####
            _bestTime = st.getTotalTime();//####[131]####
        }//####[132]####
    }//####[133]####
//####[139]####
    private static volatile Method __pt__recursiveScheduleGenerationTask_ListAlgorithmNodeImp_ListAlgorithmNodeImp_method = null;//####[139]####
    private synchronized static void __pt__recursiveScheduleGenerationTask_ListAlgorithmNodeImp_ListAlgorithmNodeImp_ensureMethodVarSet() {//####[139]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNodeImp_ListAlgorithmNodeImp_method == null) {//####[139]####
            try {//####[139]####
                __pt__recursiveScheduleGenerationTask_ListAlgorithmNodeImp_ListAlgorithmNodeImp_method = ParaTaskHelper.getDeclaredMethod(new ParaTaskHelper.ClassGetter().getCurrentClass(), "__pt__recursiveScheduleGenerationTask", new Class[] {//####[139]####
                    List.class, List.class//####[139]####
                });//####[139]####
            } catch (Exception e) {//####[139]####
                e.printStackTrace();//####[139]####
            }//####[139]####
        }//####[139]####
    }//####[139]####
    /**
     * Wraps the recursiveScheduleGeneration method with a parallel method.
     * Should only be used if there are resources available (check the semaphore)
     *///####[139]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNodeImp> processed, List<AlgorithmNodeImp> remainingNodes) {//####[139]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[139]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, new TaskInfo());//####[139]####
    }//####[139]####
    /**
     * Wraps the recursiveScheduleGeneration method with a parallel method.
     * Should only be used if there are resources available (check the semaphore)
     *///####[139]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNodeImp> processed, List<AlgorithmNodeImp> remainingNodes, TaskInfo taskinfo) {//####[139]####
        // ensure Method variable is set//####[139]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNodeImp_ListAlgorithmNodeImp_method == null) {//####[139]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNodeImp_ListAlgorithmNodeImp_ensureMethodVarSet();//####[139]####
        }//####[139]####
        taskinfo.setParameters(processed, remainingNodes);//####[139]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNodeImp_ListAlgorithmNodeImp_method);//####[139]####
        taskinfo.setInstance(this);//####[139]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[139]####
    }//####[139]####
    /**
     * Wraps the recursiveScheduleGeneration method with a parallel method.
     * Should only be used if there are resources available (check the semaphore)
     *///####[139]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNodeImp>> processed, List<AlgorithmNodeImp> remainingNodes) {//####[139]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[139]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, new TaskInfo());//####[139]####
    }//####[139]####
    /**
     * Wraps the recursiveScheduleGeneration method with a parallel method.
     * Should only be used if there are resources available (check the semaphore)
     *///####[139]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNodeImp>> processed, List<AlgorithmNodeImp> remainingNodes, TaskInfo taskinfo) {//####[139]####
        // ensure Method variable is set//####[139]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNodeImp_ListAlgorithmNodeImp_method == null) {//####[139]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNodeImp_ListAlgorithmNodeImp_ensureMethodVarSet();//####[139]####
        }//####[139]####
        taskinfo.setTaskIdArgIndexes(0);//####[139]####
        taskinfo.addDependsOn(processed);//####[139]####
        taskinfo.setParameters(processed, remainingNodes);//####[139]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNodeImp_ListAlgorithmNodeImp_method);//####[139]####
        taskinfo.setInstance(this);//####[139]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[139]####
    }//####[139]####
    /**
     * Wraps the recursiveScheduleGeneration method with a parallel method.
     * Should only be used if there are resources available (check the semaphore)
     *///####[139]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNodeImp>> processed, List<AlgorithmNodeImp> remainingNodes) {//####[139]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[139]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, new TaskInfo());//####[139]####
    }//####[139]####
    /**
     * Wraps the recursiveScheduleGeneration method with a parallel method.
     * Should only be used if there are resources available (check the semaphore)
     *///####[139]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNodeImp>> processed, List<AlgorithmNodeImp> remainingNodes, TaskInfo taskinfo) {//####[139]####
        // ensure Method variable is set//####[139]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNodeImp_ListAlgorithmNodeImp_method == null) {//####[139]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNodeImp_ListAlgorithmNodeImp_ensureMethodVarSet();//####[139]####
        }//####[139]####
        taskinfo.setQueueArgIndexes(0);//####[139]####
        taskinfo.setIsPipeline(true);//####[139]####
        taskinfo.setParameters(processed, remainingNodes);//####[139]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNodeImp_ListAlgorithmNodeImp_method);//####[139]####
        taskinfo.setInstance(this);//####[139]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[139]####
    }//####[139]####
    /**
     * Wraps the recursiveScheduleGeneration method with a parallel method.
     * Should only be used if there are resources available (check the semaphore)
     *///####[139]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNodeImp> processed, TaskID<List<AlgorithmNodeImp>> remainingNodes) {//####[139]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[139]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, new TaskInfo());//####[139]####
    }//####[139]####
    /**
     * Wraps the recursiveScheduleGeneration method with a parallel method.
     * Should only be used if there are resources available (check the semaphore)
     *///####[139]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNodeImp> processed, TaskID<List<AlgorithmNodeImp>> remainingNodes, TaskInfo taskinfo) {//####[139]####
        // ensure Method variable is set//####[139]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNodeImp_ListAlgorithmNodeImp_method == null) {//####[139]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNodeImp_ListAlgorithmNodeImp_ensureMethodVarSet();//####[139]####
        }//####[139]####
        taskinfo.setTaskIdArgIndexes(1);//####[139]####
        taskinfo.addDependsOn(remainingNodes);//####[139]####
        taskinfo.setParameters(processed, remainingNodes);//####[139]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNodeImp_ListAlgorithmNodeImp_method);//####[139]####
        taskinfo.setInstance(this);//####[139]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[139]####
    }//####[139]####
    /**
     * Wraps the recursiveScheduleGeneration method with a parallel method.
     * Should only be used if there are resources available (check the semaphore)
     *///####[139]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNodeImp>> processed, TaskID<List<AlgorithmNodeImp>> remainingNodes) {//####[139]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[139]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, new TaskInfo());//####[139]####
    }//####[139]####
    /**
     * Wraps the recursiveScheduleGeneration method with a parallel method.
     * Should only be used if there are resources available (check the semaphore)
     *///####[139]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNodeImp>> processed, TaskID<List<AlgorithmNodeImp>> remainingNodes, TaskInfo taskinfo) {//####[139]####
        // ensure Method variable is set//####[139]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNodeImp_ListAlgorithmNodeImp_method == null) {//####[139]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNodeImp_ListAlgorithmNodeImp_ensureMethodVarSet();//####[139]####
        }//####[139]####
        taskinfo.setTaskIdArgIndexes(0, 1);//####[139]####
        taskinfo.addDependsOn(processed);//####[139]####
        taskinfo.addDependsOn(remainingNodes);//####[139]####
        taskinfo.setParameters(processed, remainingNodes);//####[139]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNodeImp_ListAlgorithmNodeImp_method);//####[139]####
        taskinfo.setInstance(this);//####[139]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[139]####
    }//####[139]####
    /**
     * Wraps the recursiveScheduleGeneration method with a parallel method.
     * Should only be used if there are resources available (check the semaphore)
     *///####[139]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNodeImp>> processed, TaskID<List<AlgorithmNodeImp>> remainingNodes) {//####[139]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[139]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, new TaskInfo());//####[139]####
    }//####[139]####
    /**
     * Wraps the recursiveScheduleGeneration method with a parallel method.
     * Should only be used if there are resources available (check the semaphore)
     *///####[139]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNodeImp>> processed, TaskID<List<AlgorithmNodeImp>> remainingNodes, TaskInfo taskinfo) {//####[139]####
        // ensure Method variable is set//####[139]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNodeImp_ListAlgorithmNodeImp_method == null) {//####[139]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNodeImp_ListAlgorithmNodeImp_ensureMethodVarSet();//####[139]####
        }//####[139]####
        taskinfo.setQueueArgIndexes(0);//####[139]####
        taskinfo.setIsPipeline(true);//####[139]####
        taskinfo.setTaskIdArgIndexes(1);//####[139]####
        taskinfo.addDependsOn(remainingNodes);//####[139]####
        taskinfo.setParameters(processed, remainingNodes);//####[139]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNodeImp_ListAlgorithmNodeImp_method);//####[139]####
        taskinfo.setInstance(this);//####[139]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[139]####
    }//####[139]####
    /**
     * Wraps the recursiveScheduleGeneration method with a parallel method.
     * Should only be used if there are resources available (check the semaphore)
     *///####[139]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNodeImp> processed, BlockingQueue<List<AlgorithmNodeImp>> remainingNodes) {//####[139]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[139]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, new TaskInfo());//####[139]####
    }//####[139]####
    /**
     * Wraps the recursiveScheduleGeneration method with a parallel method.
     * Should only be used if there are resources available (check the semaphore)
     *///####[139]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNodeImp> processed, BlockingQueue<List<AlgorithmNodeImp>> remainingNodes, TaskInfo taskinfo) {//####[139]####
        // ensure Method variable is set//####[139]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNodeImp_ListAlgorithmNodeImp_method == null) {//####[139]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNodeImp_ListAlgorithmNodeImp_ensureMethodVarSet();//####[139]####
        }//####[139]####
        taskinfo.setQueueArgIndexes(1);//####[139]####
        taskinfo.setIsPipeline(true);//####[139]####
        taskinfo.setParameters(processed, remainingNodes);//####[139]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNodeImp_ListAlgorithmNodeImp_method);//####[139]####
        taskinfo.setInstance(this);//####[139]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[139]####
    }//####[139]####
    /**
     * Wraps the recursiveScheduleGeneration method with a parallel method.
     * Should only be used if there are resources available (check the semaphore)
     *///####[139]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNodeImp>> processed, BlockingQueue<List<AlgorithmNodeImp>> remainingNodes) {//####[139]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[139]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, new TaskInfo());//####[139]####
    }//####[139]####
    /**
     * Wraps the recursiveScheduleGeneration method with a parallel method.
     * Should only be used if there are resources available (check the semaphore)
     *///####[139]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNodeImp>> processed, BlockingQueue<List<AlgorithmNodeImp>> remainingNodes, TaskInfo taskinfo) {//####[139]####
        // ensure Method variable is set//####[139]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNodeImp_ListAlgorithmNodeImp_method == null) {//####[139]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNodeImp_ListAlgorithmNodeImp_ensureMethodVarSet();//####[139]####
        }//####[139]####
        taskinfo.setQueueArgIndexes(1);//####[139]####
        taskinfo.setIsPipeline(true);//####[139]####
        taskinfo.setTaskIdArgIndexes(0);//####[139]####
        taskinfo.addDependsOn(processed);//####[139]####
        taskinfo.setParameters(processed, remainingNodes);//####[139]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNodeImp_ListAlgorithmNodeImp_method);//####[139]####
        taskinfo.setInstance(this);//####[139]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[139]####
    }//####[139]####
    /**
     * Wraps the recursiveScheduleGeneration method with a parallel method.
     * Should only be used if there are resources available (check the semaphore)
     *///####[139]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNodeImp>> processed, BlockingQueue<List<AlgorithmNodeImp>> remainingNodes) {//####[139]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[139]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, new TaskInfo());//####[139]####
    }//####[139]####
    /**
     * Wraps the recursiveScheduleGeneration method with a parallel method.
     * Should only be used if there are resources available (check the semaphore)
     *///####[139]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNodeImp>> processed, BlockingQueue<List<AlgorithmNodeImp>> remainingNodes, TaskInfo taskinfo) {//####[139]####
        // ensure Method variable is set//####[139]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNodeImp_ListAlgorithmNodeImp_method == null) {//####[139]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNodeImp_ListAlgorithmNodeImp_ensureMethodVarSet();//####[139]####
        }//####[139]####
        taskinfo.setQueueArgIndexes(0, 1);//####[139]####
        taskinfo.setIsPipeline(true);//####[139]####
        taskinfo.setParameters(processed, remainingNodes);//####[139]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNodeImp_ListAlgorithmNodeImp_method);//####[139]####
        taskinfo.setInstance(this);//####[139]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[139]####
    }//####[139]####
    /**
     * Wraps the recursiveScheduleGeneration method with a parallel method.
     * Should only be used if there are resources available (check the semaphore)
     *///####[139]####
    public void __pt__recursiveScheduleGenerationTask(List<AlgorithmNodeImp> processed, List<AlgorithmNodeImp> remainingNodes) {//####[139]####
        recursiveScheduleGeneration(processed, remainingNodes);//####[140]####
        _threads.release();//####[141]####
    }//####[142]####
//####[142]####
//####[144]####
    private void setNewBestSchedule(ScheduleImp st) {//####[144]####
        for (int i = 0; i < st.getSizeOfSchedule(); i++) //####[145]####
        {//####[145]####
            NodeSchedule nodeSchedule = new NodeScheduleImp(st.getNodeStartTime(i), st.getNodeCore(i));//####[146]####
            _currentBestSchedule.put(st.getNodeName(i), nodeSchedule);//####[147]####
        }//####[150]####
    }//####[151]####
//####[160]####
    /**
	 * This method determines whether a schedule is valid. It does this by ensuring a nodes predecessors are scheduled
	 * before the current node
	 *
	 * @param schedule
	 * @return true if the schedule is valid, false if not
	 *///####[160]####
    private boolean checkValidSchedule(List<AlgorithmNodeImp> schedule) {//####[160]####
        if (schedule == null) //####[161]####
        {//####[161]####
            return false;//####[162]####
        }//####[163]####
        for (int i = 0; i < schedule.size(); i++) //####[165]####
        {//####[165]####
            Node currentNode = _dag.getNodeByName(schedule.get(i).getNodeName());//####[167]####
            List<Node> predecessors = currentNode.getPredecessors();//####[168]####
            if (predecessors.size() == 0) //####[171]####
            {//####[171]####
                continue;//####[172]####
            }//####[173]####
            int counter = 0;//####[176]####
            for (int j = i - 1; j >= 0; j--) //####[177]####
            {//####[177]####
                for (Node preNode : predecessors) //####[178]####
                {//####[178]####
                    if (schedule.get(j).getNodeName().equals(preNode.getName())) //####[179]####
                    {//####[179]####
                        counter++;//####[180]####
                        break;//####[181]####
                    }//####[182]####
                }//####[183]####
            }//####[184]####
            if (counter != predecessors.size()) //####[187]####
            {//####[187]####
                return false;//####[188]####
            }//####[189]####
        }//####[190]####
        return true;//####[191]####
    }//####[192]####
//####[199]####
    /**
	 * Calculates the time cost of executing the given schedule, returning a complete SchedulerTimeImp object.
	 * @param algNodes - A {@code List<AlgorithmNodeImp>} given in the order of execution
	 * @return - SchedulerTimeImp object with cost and execution time information
	 *///####[199]####
    private ScheduleImp calculateTotalTime(List<AlgorithmNodeImp> algNodes) {//####[199]####
        List<Node> nodes = new ArrayList<Node>();//####[201]####
        for (AlgorithmNodeImp algNode : algNodes) //####[204]####
        {//####[204]####
            nodes.add(_dag.getNodeByName(algNode.getNodeName()));//####[205]####
        }//####[206]####
        List<AlgorithmNodeImp> latestAlgNodeInSchedules = Arrays.asList(new AlgorithmNodeImp[_numberOfCores]);//####[210]####
        ScheduleImp st = new ScheduleImp(algNodes);//####[213]####
        for (AlgorithmNodeImp currentAlgNode : algNodes) //####[216]####
        {//####[216]####
            Node currentNode = nodes.get(algNodes.indexOf(currentAlgNode));//####[217]####
            int highestCost = 0;//####[218]####
            for (Node node : currentNode.getPredecessors()) //####[221]####
            {//####[221]####
                int cost = st.getNodeStartTime(getIndexOfList(node, algNodes)) + node.getWeight();//####[223]####
                if (!(algNodes.get(getIndexOfList(node, algNodes)).getCore() == currentAlgNode.getCore())) //####[224]####
                {//####[224]####
                    cost += currentNode.getInArc(node).getWeight();//####[226]####
                }//####[227]####
                if (cost > highestCost) //####[229]####
                {//####[229]####
                    highestCost = cost;//####[230]####
                }//####[231]####
            }//####[232]####
            AlgorithmNodeImp latestNode = latestAlgNodeInSchedules.get(currentAlgNode.getCore() - 1);//####[235]####
            if (latestNode != null) //####[236]####
            {//####[236]####
                Node previousNode = _dag.getNodeByName(latestNode.getNodeName());//####[237]####
                int cost = previousNode.getWeight() + st.getNodeStartTime(algNodes.indexOf(latestNode));//####[238]####
                if (cost > highestCost) //####[239]####
                {//####[239]####
                    highestCost = cost;//####[240]####
                }//####[241]####
            }//####[242]####
            latestAlgNodeInSchedules.set(currentAlgNode.getCore() - 1, currentAlgNode);//####[245]####
            st.setStartTimeForNode(highestCost, algNodes.indexOf(currentAlgNode));//####[248]####
        }//####[249]####
        setTimeForSchedulerTime(latestAlgNodeInSchedules, algNodes, st);//####[251]####
        return st;//####[253]####
    }//####[254]####
//####[263]####
    /**
	 * Calculates and sets the total time in the {@code SchedulerTimeImp} object given.
	 * Main purpose is to make the code more readable.
	 * @param latestAlgNodeInSchedules - {@code List<AlgorithmNodeImp>} containing the last node in each processor
	 * @param algNodes - the same {@code List<AlgorithmnNode>} used to construct the {@code SchedulerTimeImp} object
	 * @param st - {@code SchedulerTimeImp} object to set the total time of
	 *///####[263]####
    private void setTimeForSchedulerTime(List<AlgorithmNodeImp> latestAlgNodeInSchedules, List<AlgorithmNodeImp> algNodes, ScheduleImp st) {//####[263]####
        int totalTime = 0;//####[264]####
        for (int i = 1; i <= _numberOfCores; i++) //####[265]####
        {//####[265]####
            AlgorithmNodeImp latestAlgNode = latestAlgNodeInSchedules.get(i - 1);//####[266]####
            int timeTaken = 0;//####[268]####
            if (latestAlgNode != null) //####[269]####
            {//####[269]####
                timeTaken = st.getNodeStartTime(algNodes.indexOf(latestAlgNode)) + _dag.getNodeByName(latestAlgNode.getNodeName()).getWeight();//####[270]####
            }//####[271]####
            if (timeTaken > totalTime) //####[273]####
            {//####[273]####
                totalTime = timeTaken;//####[274]####
            }//####[275]####
        }//####[276]####
        st.setTotalTime(totalTime);//####[278]####
    }//####[279]####
//####[287]####
    /**
	 * Finds and returns the index position of the corresponding {@code AlgorithmNodeImp} within the given {@code List<AlgorithmNodeImp}
	 * @param node - {@code Node} to find the corresponding index position for
	 * @param algNodes - {@code List<AlgorithmNodeImp>} to find the index for
	 * @return the index position of the corresponding {@code AlgorithmNodeImp} object
	 *///####[287]####
    private int getIndexOfList(Node node, List<AlgorithmNodeImp> algNodes) {//####[287]####
        for (AlgorithmNodeImp algNode : algNodes) //####[288]####
        {//####[288]####
            if (node.getName().equals(algNode.getNodeName())) //####[289]####
            {//####[289]####
                return algNodes.indexOf(algNode);//####[290]####
            }//####[291]####
        }//####[292]####
        return -1;//####[293]####
    }//####[294]####
//####[297]####
    @Override//####[297]####
    public HashMap<String, NodeSchedule> getCurrentBestSchedule() {//####[297]####
        return _currentBestSchedule;//####[298]####
    }//####[299]####
//####[302]####
    @Override//####[302]####
    public int getBestTotalTime() {//####[302]####
        return _bestTime;//####[303]####
    }//####[304]####
//####[311]####
    /**
	 * The wrapper methods purely for testing. (as the methods were declared to be private)
	 * @param algNodes
	 * @return
	 *///####[311]####
    public ScheduleImp calculateTotalTimeWrapper(List<AlgorithmNodeImp> algNodes) {//####[311]####
        return calculateTotalTime(algNodes);//####[312]####
    }//####[313]####
//####[315]####
    public boolean checkValidScheduleWrapper(List<AlgorithmNodeImp> s1) {//####[315]####
        return checkValidSchedule(s1);//####[316]####
    }//####[317]####
}//####[317]####
