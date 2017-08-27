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
import java.util.stream.Collectors;//####[12]####
import java.util.concurrent.atomic.AtomicInteger;//####[13]####
import java.lang.Thread;//####[14]####
import java.util.concurrent.Semaphore;//####[16]####
import java.lang.InterruptedException;//####[17]####
import implementations.structures.DAGImp;//####[19]####
import implementations.structures.NodeScheduleImp;//####[20]####
import implementations.structures.ScheduleImp;//####[21]####
import interfaces.algorithm.Algorithm;//####[22]####
import interfaces.algorithm.AlgorithmNode;//####[23]####
import interfaces.structures.DAG;//####[24]####
import interfaces.structures.Node;//####[25]####
import interfaces.structures.NodeSchedule;//####[26]####
import interfaces.structures.Schedule;//####[27]####
import visualisation.BarChartModel;//####[28]####
import visualisation.Clock;//####[29]####
import visualisation.ComboView;//####[30]####
import visualisation.TableModel;//####[31]####
//####[31]####
//-- ParaTask related imports//####[31]####
import pt.runtime.*;//####[31]####
import java.util.concurrent.ExecutionException;//####[31]####
import java.util.concurrent.locks.*;//####[31]####
import java.lang.reflect.*;//####[31]####
import pt.runtime.GuiThread;//####[31]####
import java.util.concurrent.BlockingQueue;//####[31]####
import java.util.ArrayList;//####[31]####
import java.util.List;//####[31]####
//####[31]####
/**
 * This class represents the algorithm to solve the scheduling problem.
 * The class is responsible for all DFS searches and maintaining a current best result.
 * The class also acts as a controller for the View to update the visualisation.
 * 
 * Algorithm @author: Daniel, Victor, Wayne
 * 
 * Visualisation @author: Pulkit
 *///####[41]####
public class AlgorithmImp implements Algorithm {//####[42]####
    static{ParaTask.init();}//####[42]####
    /*  ParaTask helper method to access private/protected slots *///####[42]####
    public void __pt__accessPrivateSlot(Method m, Object instance, TaskID arg, Object interResult ) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {//####[42]####
        if (m.getParameterTypes().length == 0)//####[42]####
            m.invoke(instance);//####[42]####
        else if ((m.getParameterTypes().length == 1))//####[42]####
            m.invoke(instance, arg);//####[42]####
        else //####[42]####
            m.invoke(instance, arg, interResult);//####[42]####
    }//####[42]####
//####[43]####
    private DAG _dag;//####[43]####
//####[44]####
    private int _numberOfCores;//####[44]####
//####[45]####
    private HashMap<String, NodeSchedule> _currentBestSchedule;//####[45]####
//####[46]####
    private AtomicInteger _recursiveCalls;//####[46]####
//####[47]####
    private final Semaphore _threads;//####[47]####
//####[48]####
    private final int _numberOfThreads = 3;//####[48]####
//####[50]####
    private TableModel _model;//####[50]####
//####[51]####
    private BarChartModel _chartModel;//####[51]####
//####[53]####
    private int _bestTime = Integer.MAX_VALUE;//####[53]####
//####[55]####
    private Set<Set<AlgorithmNode>> _uniqueProcessed;//####[55]####
//####[57]####
    boolean visualisation = true;//####[57]####
//####[59]####
    private AtomicInteger _switchingCount;//####[59]####
//####[61]####
    public AlgorithmImp(int numberOfCores) {//####[61]####
        _switchingCount = new AtomicInteger();//####[62]####
        _recursiveCalls = new AtomicInteger();//####[63]####
        _dag = DAGImp.getInstance();//####[64]####
        _numberOfCores = numberOfCores;//####[65]####
        _currentBestSchedule = new HashMap<String, NodeSchedule>();//####[66]####
        if (visualisation) //####[68]####
        {//####[68]####
            _model = TableModel.getInstance();//####[70]####
            _model.initModel(_currentBestSchedule, _dag, _numberOfCores);//####[71]####
            _chartModel = new BarChartModel();//####[73]####
            ComboView schedule = new ComboView(_model, _dag, _numberOfCores, _chartModel);//####[75]####
        }//####[76]####
        _uniqueProcessed = Collections.synchronizedSet(new HashSet<Set<AlgorithmNode>>());//####[78]####
        produceSequentialSchedule();//####[80]####
        produceGreedySchedule();//####[81]####
        _threads = new Semaphore(_numberOfThreads);//####[83]####
        Schedule emptySchedule = new ScheduleImp(_numberOfCores);//####[85]####
        recursiveScheduleGeneration(new ArrayList<AlgorithmNode>(), AlgorithmNode.convertNodetoAlgorithmNode(_dag.getAllNodes()), emptySchedule);//####[86]####
        try {//####[88]####
            _threads.acquire(_numberOfThreads);//####[89]####
        } catch (InterruptedException ex) {//####[90]####
            ex.printStackTrace();//####[91]####
        }//####[92]####
        if (visualisation) //####[94]####
        {//####[94]####
            _model.changeData(_currentBestSchedule, _bestTime);//####[95]####
            _model = TableModel.setInstance();//####[97]####
        }//####[98]####
        System.out.println("Switched " + _switchingCount.get() + " times");//####[100]####
    }//####[101]####
//####[106]####
    /**
	 * helper method for firing update.
	 *///####[106]####
    private void fireUpdateToGUI() {//####[106]####
        _chartModel.addDataToSeries(_bestTime);//####[111]####
        int timeNow = Clock.getInstance().getMilliseconds();//####[112]####
        Clock.lastUpdate = timeNow;//####[114]####
        _model.changeData(_currentBestSchedule, _bestTime);//####[115]####
    }//####[116]####
//####[124]####
    /**
	 * This method will produce a sequential schedule to set the lower bound.
	 * 
	 * This will be used together will the greedy schedule to bound
	 * the DFS.
	 *///####[124]####
    private void produceSequentialSchedule() {//####[124]####
        List<Node> reachableNodes = new ArrayList<Node>();//####[125]####
        List<Node> completedNodes = new ArrayList<Node>();//####[126]####
        List<Node> remainingNodes = new ArrayList<Node>();//####[127]####
        reachableNodes.addAll(_dag.getStartNodes());//####[129]####
        remainingNodes.addAll(_dag.getAllNodes());//####[130]####
        Schedule schedule = new ScheduleImp(_numberOfCores);//####[132]####
        while (!reachableNodes.isEmpty()) //####[134]####
        {//####[134]####
            Node toBeScheduled = reachableNodes.get(0);//####[135]####
            AlgorithmNode algNode = new AlgorithmNodeImp(toBeScheduled.getName());//####[137]####
            algNode.setCore(1);//####[138]####
            schedule = schedule.getNextSchedule(algNode);//####[139]####
            completedNodes.add(toBeScheduled);//####[142]####
            reachableNodes.remove(toBeScheduled);//####[143]####
            remainingNodes.remove(toBeScheduled);//####[144]####
            for (Node rn : remainingNodes) //####[145]####
            {//####[145]####
                if (completedNodes.containsAll(rn.getPredecessors()) && !reachableNodes.contains(rn)) //####[146]####
                {//####[146]####
                    reachableNodes.add(rn);//####[147]####
                }//####[148]####
            }//####[149]####
        }//####[150]####
        setNewBestSchedule(schedule);//####[153]####
        _bestTime = schedule.getTotalTime();//####[154]####
        if (visualisation) //####[156]####
        {//####[156]####
            fireUpdateToGUI();//####[157]####
        }//####[158]####
    }//####[159]####
//####[167]####
    /**
	 * This method will produce a greedy schedule to set the lower bound.
	 * 
	 * This will be used together will the sequential schedule to bound
	 * the DFS.
	 *///####[167]####
    private void produceGreedySchedule() {//####[167]####
        List<Node> reachableNodes = new ArrayList<Node>();//####[168]####
        List<Node> completedNodes = new ArrayList<Node>();//####[169]####
        List<Node> remainingNodes = new ArrayList<Node>();//####[170]####
        reachableNodes.addAll(_dag.getStartNodes());//####[172]####
        remainingNodes.addAll(_dag.getAllNodes());//####[173]####
        Schedule schedule = new ScheduleImp(_numberOfCores);//####[175]####
        while (!reachableNodes.isEmpty()) //####[177]####
        {//####[177]####
            List<Integer> reachableAmount = new ArrayList<Integer>();//####[179]####
            for (Node n : reachableNodes) //####[180]####
            {//####[180]####
                reachableAmount.add(n.getSuccessors().size());//####[181]####
            }//####[182]####
            int maxIndex = reachableAmount.indexOf(Collections.max(reachableAmount));//####[183]####
            Node toBeScheduled = reachableNodes.get(maxIndex);//####[184]####
            List<Integer> earliestStartTimes = new ArrayList<Integer>();//####[187]####
            for (int i = 1; i <= _numberOfCores; i++) //####[188]####
            {//####[188]####
                int coreStart = schedule.getFinishTimeForCore(i);//####[189]####
                AlgorithmNode algNode = new AlgorithmNodeImp(toBeScheduled.getName());//####[190]####
                algNode.setCore(i);//####[191]####
                int depStart = schedule.getDependencyBasedStartTime(toBeScheduled, algNode);//####[192]####
                earliestStartTimes.add((coreStart > depStart) ? coreStart : depStart);//####[193]####
            }//####[194]####
            int earliestCoreNo = earliestStartTimes.indexOf(Collections.min(earliestStartTimes)) + 1;//####[195]####
            AlgorithmNode algNode = new AlgorithmNodeImp(toBeScheduled.getName());//####[197]####
            algNode.setCore(earliestCoreNo);//####[198]####
            schedule = schedule.getNextSchedule(algNode);//####[199]####
            completedNodes.add(toBeScheduled);//####[202]####
            reachableNodes.remove(toBeScheduled);//####[203]####
            remainingNodes.remove(toBeScheduled);//####[204]####
            for (Node rn : remainingNodes) //####[205]####
            {//####[205]####
                if (completedNodes.containsAll(rn.getPredecessors()) && !reachableNodes.contains(rn)) //####[206]####
                {//####[206]####
                    reachableNodes.add(rn);//####[207]####
                }//####[208]####
            }//####[209]####
        }//####[210]####
        if (schedule.getTotalTime() < _bestTime) //####[212]####
        {//####[212]####
            setNewBestSchedule(schedule);//####[213]####
            _bestTime = schedule.getTotalTime();//####[214]####
            if (visualisation) //####[216]####
            {//####[216]####
                fireUpdateToGUI();//####[217]####
            }//####[218]####
        }//####[219]####
    }//####[220]####
//####[227]####
    /**
	 * Purely for benchmarking purposes
	 *
	 * @return number of times the recursive method was called
	 *///####[227]####
    public int getRecursiveCalls() {//####[227]####
        return _recursiveCalls.get();//####[228]####
    }//####[229]####
//####[231]####
    private synchronized void compareSchedules(Schedule s) {//####[231]####
        if (s.getTotalTime() < _bestTime) //####[232]####
        {//####[232]####
            setNewBestSchedule(s);//####[233]####
            _bestTime = s.getTotalTime();//####[234]####
            if (visualisation) //####[236]####
            {//####[236]####
                fireUpdateToGUI();//####[237]####
            }//####[238]####
        }//####[239]####
    }//####[240]####
//####[242]####
    private static volatile Method __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method = null;//####[242]####
    private synchronized static void __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet() {//####[242]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[242]####
            try {//####[242]####
                __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method = ParaTaskHelper.getDeclaredMethod(new ParaTaskHelper.ClassGetter().getCurrentClass(), "__pt__recursiveScheduleGenerationTask", new Class[] {//####[242]####
                    List.class, List.class, Schedule.class//####[242]####
                });//####[242]####
            } catch (Exception e) {//####[242]####
                e.printStackTrace();//####[242]####
            }//####[242]####
        }//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, Schedule prev) {//####[242]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[242]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[242]####
        // ensure Method variable is set//####[242]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[242]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[242]####
        }//####[242]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[242]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[242]####
        taskinfo.setInstance(this);//####[242]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, Schedule prev) {//####[242]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[242]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[242]####
        // ensure Method variable is set//####[242]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[242]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[242]####
        }//####[242]####
        taskinfo.setTaskIdArgIndexes(0);//####[242]####
        taskinfo.addDependsOn(processed);//####[242]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[242]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[242]####
        taskinfo.setInstance(this);//####[242]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, Schedule prev) {//####[242]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[242]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[242]####
        // ensure Method variable is set//####[242]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[242]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[242]####
        }//####[242]####
        taskinfo.setQueueArgIndexes(0);//####[242]####
        taskinfo.setIsPipeline(true);//####[242]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[242]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[242]####
        taskinfo.setInstance(this);//####[242]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, TaskID<List<AlgorithmNode>> remainingNodes, Schedule prev) {//####[242]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[242]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, TaskID<List<AlgorithmNode>> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[242]####
        // ensure Method variable is set//####[242]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[242]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[242]####
        }//####[242]####
        taskinfo.setTaskIdArgIndexes(1);//####[242]####
        taskinfo.addDependsOn(remainingNodes);//####[242]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[242]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[242]####
        taskinfo.setInstance(this);//####[242]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, Schedule prev) {//####[242]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[242]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[242]####
        // ensure Method variable is set//####[242]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[242]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[242]####
        }//####[242]####
        taskinfo.setTaskIdArgIndexes(0, 1);//####[242]####
        taskinfo.addDependsOn(processed);//####[242]####
        taskinfo.addDependsOn(remainingNodes);//####[242]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[242]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[242]####
        taskinfo.setInstance(this);//####[242]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, Schedule prev) {//####[242]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[242]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[242]####
        // ensure Method variable is set//####[242]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[242]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[242]####
        }//####[242]####
        taskinfo.setQueueArgIndexes(0);//####[242]####
        taskinfo.setIsPipeline(true);//####[242]####
        taskinfo.setTaskIdArgIndexes(1);//####[242]####
        taskinfo.addDependsOn(remainingNodes);//####[242]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[242]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[242]####
        taskinfo.setInstance(this);//####[242]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, Schedule prev) {//####[242]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[242]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[242]####
        // ensure Method variable is set//####[242]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[242]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[242]####
        }//####[242]####
        taskinfo.setQueueArgIndexes(1);//####[242]####
        taskinfo.setIsPipeline(true);//####[242]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[242]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[242]####
        taskinfo.setInstance(this);//####[242]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, Schedule prev) {//####[242]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[242]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[242]####
        // ensure Method variable is set//####[242]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[242]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[242]####
        }//####[242]####
        taskinfo.setQueueArgIndexes(1);//####[242]####
        taskinfo.setIsPipeline(true);//####[242]####
        taskinfo.setTaskIdArgIndexes(0);//####[242]####
        taskinfo.addDependsOn(processed);//####[242]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[242]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[242]####
        taskinfo.setInstance(this);//####[242]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, Schedule prev) {//####[242]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[242]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[242]####
        // ensure Method variable is set//####[242]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[242]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[242]####
        }//####[242]####
        taskinfo.setQueueArgIndexes(0, 1);//####[242]####
        taskinfo.setIsPipeline(true);//####[242]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[242]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[242]####
        taskinfo.setInstance(this);//####[242]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, TaskID<Schedule> prev) {//####[242]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[242]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[242]####
        // ensure Method variable is set//####[242]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[242]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[242]####
        }//####[242]####
        taskinfo.setTaskIdArgIndexes(2);//####[242]####
        taskinfo.addDependsOn(prev);//####[242]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[242]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[242]####
        taskinfo.setInstance(this);//####[242]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, TaskID<Schedule> prev) {//####[242]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[242]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[242]####
        // ensure Method variable is set//####[242]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[242]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[242]####
        }//####[242]####
        taskinfo.setTaskIdArgIndexes(0, 2);//####[242]####
        taskinfo.addDependsOn(processed);//####[242]####
        taskinfo.addDependsOn(prev);//####[242]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[242]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[242]####
        taskinfo.setInstance(this);//####[242]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, TaskID<Schedule> prev) {//####[242]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[242]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[242]####
        // ensure Method variable is set//####[242]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[242]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[242]####
        }//####[242]####
        taskinfo.setQueueArgIndexes(0);//####[242]####
        taskinfo.setIsPipeline(true);//####[242]####
        taskinfo.setTaskIdArgIndexes(2);//####[242]####
        taskinfo.addDependsOn(prev);//####[242]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[242]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[242]####
        taskinfo.setInstance(this);//####[242]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, TaskID<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev) {//####[242]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[242]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, TaskID<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[242]####
        // ensure Method variable is set//####[242]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[242]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[242]####
        }//####[242]####
        taskinfo.setTaskIdArgIndexes(1, 2);//####[242]####
        taskinfo.addDependsOn(remainingNodes);//####[242]####
        taskinfo.addDependsOn(prev);//####[242]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[242]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[242]####
        taskinfo.setInstance(this);//####[242]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev) {//####[242]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[242]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[242]####
        // ensure Method variable is set//####[242]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[242]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[242]####
        }//####[242]####
        taskinfo.setTaskIdArgIndexes(0, 1, 2);//####[242]####
        taskinfo.addDependsOn(processed);//####[242]####
        taskinfo.addDependsOn(remainingNodes);//####[242]####
        taskinfo.addDependsOn(prev);//####[242]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[242]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[242]####
        taskinfo.setInstance(this);//####[242]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev) {//####[242]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[242]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[242]####
        // ensure Method variable is set//####[242]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[242]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[242]####
        }//####[242]####
        taskinfo.setQueueArgIndexes(0);//####[242]####
        taskinfo.setIsPipeline(true);//####[242]####
        taskinfo.setTaskIdArgIndexes(1, 2);//####[242]####
        taskinfo.addDependsOn(remainingNodes);//####[242]####
        taskinfo.addDependsOn(prev);//####[242]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[242]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[242]####
        taskinfo.setInstance(this);//####[242]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev) {//####[242]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[242]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[242]####
        // ensure Method variable is set//####[242]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[242]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[242]####
        }//####[242]####
        taskinfo.setQueueArgIndexes(1);//####[242]####
        taskinfo.setIsPipeline(true);//####[242]####
        taskinfo.setTaskIdArgIndexes(2);//####[242]####
        taskinfo.addDependsOn(prev);//####[242]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[242]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[242]####
        taskinfo.setInstance(this);//####[242]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev) {//####[242]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[242]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[242]####
        // ensure Method variable is set//####[242]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[242]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[242]####
        }//####[242]####
        taskinfo.setQueueArgIndexes(1);//####[242]####
        taskinfo.setIsPipeline(true);//####[242]####
        taskinfo.setTaskIdArgIndexes(0, 2);//####[242]####
        taskinfo.addDependsOn(processed);//####[242]####
        taskinfo.addDependsOn(prev);//####[242]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[242]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[242]####
        taskinfo.setInstance(this);//####[242]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev) {//####[242]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[242]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[242]####
        // ensure Method variable is set//####[242]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[242]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[242]####
        }//####[242]####
        taskinfo.setQueueArgIndexes(0, 1);//####[242]####
        taskinfo.setIsPipeline(true);//####[242]####
        taskinfo.setTaskIdArgIndexes(2);//####[242]####
        taskinfo.addDependsOn(prev);//####[242]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[242]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[242]####
        taskinfo.setInstance(this);//####[242]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, BlockingQueue<Schedule> prev) {//####[242]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[242]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[242]####
        // ensure Method variable is set//####[242]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[242]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[242]####
        }//####[242]####
        taskinfo.setQueueArgIndexes(2);//####[242]####
        taskinfo.setIsPipeline(true);//####[242]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[242]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[242]####
        taskinfo.setInstance(this);//####[242]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, BlockingQueue<Schedule> prev) {//####[242]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[242]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[242]####
        // ensure Method variable is set//####[242]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[242]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[242]####
        }//####[242]####
        taskinfo.setQueueArgIndexes(2);//####[242]####
        taskinfo.setIsPipeline(true);//####[242]####
        taskinfo.setTaskIdArgIndexes(0);//####[242]####
        taskinfo.addDependsOn(processed);//####[242]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[242]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[242]####
        taskinfo.setInstance(this);//####[242]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, BlockingQueue<Schedule> prev) {//####[242]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[242]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[242]####
        // ensure Method variable is set//####[242]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[242]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[242]####
        }//####[242]####
        taskinfo.setQueueArgIndexes(0, 2);//####[242]####
        taskinfo.setIsPipeline(true);//####[242]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[242]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[242]####
        taskinfo.setInstance(this);//####[242]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, TaskID<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev) {//####[242]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[242]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, TaskID<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[242]####
        // ensure Method variable is set//####[242]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[242]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[242]####
        }//####[242]####
        taskinfo.setQueueArgIndexes(2);//####[242]####
        taskinfo.setIsPipeline(true);//####[242]####
        taskinfo.setTaskIdArgIndexes(1);//####[242]####
        taskinfo.addDependsOn(remainingNodes);//####[242]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[242]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[242]####
        taskinfo.setInstance(this);//####[242]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev) {//####[242]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[242]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[242]####
        // ensure Method variable is set//####[242]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[242]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[242]####
        }//####[242]####
        taskinfo.setQueueArgIndexes(2);//####[242]####
        taskinfo.setIsPipeline(true);//####[242]####
        taskinfo.setTaskIdArgIndexes(0, 1);//####[242]####
        taskinfo.addDependsOn(processed);//####[242]####
        taskinfo.addDependsOn(remainingNodes);//####[242]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[242]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[242]####
        taskinfo.setInstance(this);//####[242]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev) {//####[242]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[242]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[242]####
        // ensure Method variable is set//####[242]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[242]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[242]####
        }//####[242]####
        taskinfo.setQueueArgIndexes(0, 2);//####[242]####
        taskinfo.setIsPipeline(true);//####[242]####
        taskinfo.setTaskIdArgIndexes(1);//####[242]####
        taskinfo.addDependsOn(remainingNodes);//####[242]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[242]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[242]####
        taskinfo.setInstance(this);//####[242]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev) {//####[242]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[242]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[242]####
        // ensure Method variable is set//####[242]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[242]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[242]####
        }//####[242]####
        taskinfo.setQueueArgIndexes(1, 2);//####[242]####
        taskinfo.setIsPipeline(true);//####[242]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[242]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[242]####
        taskinfo.setInstance(this);//####[242]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev) {//####[242]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[242]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[242]####
        // ensure Method variable is set//####[242]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[242]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[242]####
        }//####[242]####
        taskinfo.setQueueArgIndexes(1, 2);//####[242]####
        taskinfo.setIsPipeline(true);//####[242]####
        taskinfo.setTaskIdArgIndexes(0);//####[242]####
        taskinfo.addDependsOn(processed);//####[242]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[242]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[242]####
        taskinfo.setInstance(this);//####[242]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev) {//####[242]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[242]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[242]####
    }//####[242]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[242]####
        // ensure Method variable is set//####[242]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[242]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[242]####
        }//####[242]####
        taskinfo.setQueueArgIndexes(0, 1, 2);//####[242]####
        taskinfo.setIsPipeline(true);//####[242]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[242]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[242]####
        taskinfo.setInstance(this);//####[242]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[242]####
    }//####[242]####
    public void __pt__recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, Schedule prev) {//####[242]####
        _switchingCount.incrementAndGet();//####[243]####
        recursiveScheduleGeneration(processed, remainingNodes, prev);//####[244]####
        _threads.release();//####[245]####
    }//####[246]####
//####[246]####
//####[260]####
    /**
	 * This method recursively does the branch and bound traversal.
	 * It takes the list of processed, remaining and previous schedule and from there determines if we need to keep going
	 * or checking if it's better than the current time.
	 *
	 * Branch down by adding each node to all the cores and then branching. Check times against heuristics and best time
	 * to decide whether to bound.
	 *
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[260]####
    private void recursiveScheduleGeneration(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, Schedule prev) {//####[260]####
        _recursiveCalls.incrementAndGet();//####[261]####
        if (remainingNodes.size() == 0) //####[264]####
        {//####[264]####
            Schedule finalSchedule = prev;//####[265]####
            compareSchedules(finalSchedule);//####[268]####
        } else {//####[269]####
            for (int i = 0; i < remainingNodes.size(); i++) //####[270]####
            {//####[270]####
                Schedule newSchedule;//####[271]####
                for (int j = 1; j <= _numberOfCores; j++) //####[274]####
                {//####[274]####
                    List<AlgorithmNode> newProcessed = new ArrayList<AlgorithmNode>(processed);//####[278]####
                    AlgorithmNode node = remainingNodes.get(i).createClone();//####[279]####
                    node.setCore(j);//####[280]####
                    newProcessed.add(node);//####[281]####
                    Set<AlgorithmNode> algNodesSet = new HashSet<AlgorithmNode>(newProcessed);//####[283]####
                    if (checkValidSchedule(newProcessed)) //####[285]####
                    {//####[285]####
                        newSchedule = prev.getNextSchedule(node);//####[286]####
                        if ((newSchedule.getTotalTime() >= _bestTime)) //####[289]####
                        {//####[289]####
                            continue;//####[290]####
                        }//####[291]####
                    } else {//####[292]####
                        break;//####[293]####
                    }//####[294]####
                    if (_uniqueProcessed.contains(algNodesSet)) //####[305]####
                    {//####[305]####
                        continue;//####[306]####
                    } else {//####[308]####
                        _uniqueProcessed.add(algNodesSet);//####[309]####
                    }//####[310]####
                    List<AlgorithmNode> newRemaining = new ArrayList<AlgorithmNode>(remainingNodes);//####[313]####
                    newRemaining.remove(i);//####[314]####
                    List<Integer> coresAssigned = new ArrayList<Integer>();//####[329]####
                    for (AlgorithmNode algNode : processed) //####[330]####
                    {//####[330]####
                        coresAssigned.add(algNode.getCore());//####[331]####
                    }//####[332]####
                    if (!coresAssigned.contains(node.getCore())) //####[334]####
                    {//####[334]####
                        recursiveScheduleGeneration(newProcessed, newRemaining, newSchedule);//####[335]####
                        break;//####[336]####
                    } else {//####[337]####
                        if (_dag.getNodeByName(node.getNodeName()).getSuccessors().size() > 1 && _threads.tryAcquire()) //####[338]####
                        {//####[338]####
                            recursiveScheduleGenerationTask(newProcessed, newRemaining, newSchedule);//####[339]####
                        } else {//####[340]####
                            recursiveScheduleGeneration(newProcessed, newRemaining, newSchedule);//####[341]####
                        }//####[342]####
                    }//####[343]####
                }//####[344]####
            }//####[345]####
        }//####[346]####
    }//####[347]####
//####[349]####
    private void setNewBestSchedule(Schedule finalSchedule) {//####[349]####
        for (int i = 0; i < finalSchedule.getSizeOfSchedule(); i++) //####[350]####
        {//####[350]####
            NodeSchedule nodeSchedule = new NodeScheduleImp(finalSchedule.getNodeStartTime(i), finalSchedule.getNodeCore(i));//####[351]####
            _currentBestSchedule.put(finalSchedule.getNodeName(i), nodeSchedule);//####[352]####
        }//####[355]####
    }//####[356]####
//####[365]####
    /**
	 * This method determines whether a schedule is valid. It does this by ensuring a nodes predecessors are scheduled
	 * before the current node
	 *
	 * @param schedule
	 * @return true if the schedule is valid, false if not
	 *///####[365]####
    private boolean checkValidSchedule(List<AlgorithmNode> schedule) {//####[365]####
        if (schedule == null) //####[366]####
        {//####[366]####
            return false;//####[367]####
        }//####[368]####
        Node currentNode = _dag.getNodeByName(schedule.get(schedule.size() - 1).getNodeName());//####[371]####
        List<Node> predecessors = currentNode.getPredecessors();//####[372]####
        if (predecessors.size() == 0) //####[375]####
        {//####[375]####
            return true;//####[376]####
        } else if (schedule.size() == 1) //####[377]####
        {//####[377]####
            return false;//####[378]####
        }//####[379]####
        int counter = 0;//####[382]####
        for (int i = schedule.size() - 2; i >= 0; i--) //####[383]####
        {//####[383]####
            for (Node preNode : predecessors) //####[384]####
            {//####[384]####
                if (schedule.get(i).getNodeName().equals(preNode.getName())) //####[385]####
                {//####[385]####
                    counter++;//####[386]####
                    break;//####[387]####
                }//####[388]####
            }//####[389]####
        }//####[390]####
        if (counter != predecessors.size()) //####[393]####
        {//####[393]####
            return false;//####[394]####
        }//####[395]####
        return true;//####[396]####
    }//####[397]####
//####[405]####
    /**
	 * Calculates the time cost of executing the given schedule, returning a complete ScheduleImp object.
	 * @param algNodes - A {@code List<AlgorithmNode>} given in the order of execution
	 * @return - ScheduleImp object with cost and execution time information
	 *///####[405]####
    @Deprecated//####[405]####
    private ScheduleImp calculateTotalTime(List<AlgorithmNode> algNodes) {//####[405]####
        List<Node> nodes = new ArrayList<Node>();//####[407]####
        for (AlgorithmNode algNode : algNodes) //####[410]####
        {//####[410]####
            nodes.add(_dag.getNodeByName(algNode.getNodeName()));//####[411]####
        }//####[412]####
        List<AlgorithmNode> latestAlgNodeInSchedules = Arrays.asList(new AlgorithmNode[_numberOfCores]);//####[416]####
        ScheduleImp st = new ScheduleImp(algNodes, _numberOfCores);//####[419]####
        for (AlgorithmNode currentAlgNode : algNodes) //####[422]####
        {//####[422]####
            Node currentNode = nodes.get(algNodes.indexOf(currentAlgNode));//####[423]####
            int highestCost = 0;//####[424]####
            for (Node node : currentNode.getPredecessors()) //####[427]####
            {//####[427]####
                int cost = st.getNodeStartTime(getIndexOfList(node, algNodes)) + node.getWeight();//####[429]####
                if (!(algNodes.get(getIndexOfList(node, algNodes)).getCore() == currentAlgNode.getCore())) //####[430]####
                {//####[430]####
                    cost += currentNode.getInArc(node).getWeight();//####[432]####
                }//####[433]####
                if (cost > highestCost) //####[435]####
                {//####[435]####
                    highestCost = cost;//####[436]####
                }//####[437]####
            }//####[438]####
            AlgorithmNode latestNode = latestAlgNodeInSchedules.get(currentAlgNode.getCore() - 1);//####[441]####
            if (latestNode != null) //####[442]####
            {//####[442]####
                Node previousNode = _dag.getNodeByName(latestNode.getNodeName());//####[443]####
                int cost = previousNode.getWeight() + st.getNodeStartTime(algNodes.indexOf(latestNode));//####[444]####
                if (cost > highestCost) //####[445]####
                {//####[445]####
                    highestCost = cost;//####[446]####
                }//####[447]####
            }//####[448]####
            latestAlgNodeInSchedules.set(currentAlgNode.getCore() - 1, currentAlgNode);//####[451]####
            st.setStartTimeForNode(highestCost, algNodes.indexOf(currentAlgNode));//####[454]####
        }//####[455]####
        setTimeForSchedule(latestAlgNodeInSchedules, algNodes, st);//####[457]####
        return st;//####[459]####
    }//####[460]####
//####[470]####
    /**
	 * Calculates and sets the total time in the {@code ScheduleImp} object given.
	 * Main purpose is to make the code more readable.
	 * @param latestAlgNodeInSchedules - {@code List<AlgorithmNode>} containing the last node in each processor
	 * @param algNodes - the same {@code List<AlgorithmnNode>} used to construct the {@code ScheduleImp} object
	 * @param st - {@code ScheduleImp} object to set the total time of
	 *///####[470]####
    @Deprecated//####[470]####
    private void setTimeForSchedule(List<AlgorithmNode> latestAlgNodeInSchedules, List<AlgorithmNode> algNodes, ScheduleImp st) {//####[470]####
        int totalTime = 0;//####[471]####
        for (int i = 1; i <= _numberOfCores; i++) //####[472]####
        {//####[472]####
            AlgorithmNode latestAlgNode = latestAlgNodeInSchedules.get(i - 1);//####[473]####
            int timeTaken = 0;//####[475]####
            if (latestAlgNode != null) //####[476]####
            {//####[476]####
                timeTaken = st.getNodeStartTime(algNodes.indexOf(latestAlgNode)) + _dag.getNodeByName(latestAlgNode.getNodeName()).getWeight();//####[477]####
            }//####[478]####
            if (timeTaken > totalTime) //####[480]####
            {//####[480]####
                totalTime = timeTaken;//####[481]####
            }//####[482]####
        }//####[483]####
        st.setTotalTime(totalTime);//####[485]####
    }//####[486]####
//####[495]####
    /**
	 * Finds and returns the index position of the corresponding {@code AlgorithmNode} within the given {@code List<AlgorithmNode}
	 * @param node - {@code Node} to find the corresponding index position for
	 * @param algNodes - {@code List<AlgorithmNode>} to find the index for
	 * @return the index position of the corresponding {@code AlgorithmNode} object
	 *///####[495]####
    @Deprecated//####[495]####
    private int getIndexOfList(Node node, List<AlgorithmNode> algNodes) {//####[495]####
        for (AlgorithmNode algNode : algNodes) //####[496]####
        {//####[496]####
            if (node.getName().equals(algNode.getNodeName())) //####[497]####
            {//####[497]####
                return algNodes.indexOf(algNode);//####[498]####
            }//####[499]####
        }//####[500]####
        return -1;//####[501]####
    }//####[502]####
//####[505]####
    @Override//####[505]####
    public HashMap<String, NodeSchedule> getCurrentBestSchedule() {//####[505]####
        return _currentBestSchedule;//####[506]####
    }//####[507]####
//####[510]####
    @Override//####[510]####
    public int getBestTotalTime() {//####[510]####
        return _bestTime;//####[511]####
    }//####[512]####
//####[520]####
    /**
	 * The wrapper methods purely for testing. (as the methods were declared to be private)
	 * @param algNodes
	 * @return
	 *///####[520]####
    @Deprecated//####[520]####
    public ScheduleImp calculateTotalTimeWrapper(List<AlgorithmNode> algNodes) {//####[520]####
        return calculateTotalTime(algNodes);//####[521]####
    }//####[522]####
//####[524]####
    public boolean checkValidScheduleWrapper(List<AlgorithmNode> s1) {//####[524]####
        return checkValidSchedule(s1);//####[525]####
    }//####[526]####
}//####[526]####
