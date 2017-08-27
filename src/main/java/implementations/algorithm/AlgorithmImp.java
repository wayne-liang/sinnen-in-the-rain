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
import java.util.concurrent.Semaphore;//####[14]####
import java.lang.InterruptedException;//####[15]####
import implementations.structures.DAGImp;//####[17]####
import implementations.structures.NodeScheduleImp;//####[18]####
import implementations.structures.ScheduleImp;//####[19]####
import interfaces.algorithm.Algorithm;//####[20]####
import interfaces.algorithm.AlgorithmNode;//####[21]####
import interfaces.structures.DAG;//####[22]####
import interfaces.structures.Node;//####[23]####
import interfaces.structures.NodeSchedule;//####[24]####
import interfaces.structures.Schedule;//####[25]####
import visualisation.BarChartModel;//####[26]####
import visualisation.Clock;//####[27]####
import visualisation.ComboView;//####[28]####
import visualisation.TableModel;//####[29]####
//####[29]####
//-- ParaTask related imports//####[29]####
import pt.runtime.*;//####[29]####
import java.util.concurrent.ExecutionException;//####[29]####
import java.util.concurrent.locks.*;//####[29]####
import java.lang.reflect.*;//####[29]####
import pt.runtime.GuiThread;//####[29]####
import java.util.concurrent.BlockingQueue;//####[29]####
import java.util.ArrayList;//####[29]####
import java.util.List;//####[29]####
//####[29]####
/**
 * This class represents the algorithm to solve the scheduling problem.
 * The class is responsible for all DFS searches and maintaining a current best result.
 * The class also acts as a controller for the View to update the visualisation.
 * 
 * Algorithm @author: Daniel, Victor, Wayne
 * 
 * Visualisation @author: Pulkit
 *///####[39]####
public class AlgorithmImp implements Algorithm {//####[40]####
    static{ParaTask.init();}//####[40]####
    /*  ParaTask helper method to access private/protected slots *///####[40]####
    public void __pt__accessPrivateSlot(Method m, Object instance, TaskID arg, Object interResult ) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {//####[40]####
        if (m.getParameterTypes().length == 0)//####[40]####
            m.invoke(instance);//####[40]####
        else if ((m.getParameterTypes().length == 1))//####[40]####
            m.invoke(instance, arg);//####[40]####
        else //####[40]####
            m.invoke(instance, arg, interResult);//####[40]####
    }//####[40]####
//####[41]####
    private DAG _dag;//####[41]####
//####[42]####
    private int _numberOfCores;//####[42]####
//####[43]####
    private HashMap<String, NodeSchedule> _currentBestSchedule;//####[43]####
//####[44]####
    private int _recursiveCalls = 0;//####[44]####
//####[45]####
    private final Semaphore _threads;//####[45]####
//####[46]####
    private final int _numberOfThreads = 16;//####[46]####
//####[48]####
    private TableModel _model;//####[48]####
//####[49]####
    private BarChartModel _chartModel;//####[49]####
//####[51]####
    private int _bestTime = Integer.MAX_VALUE;//####[51]####
//####[53]####
    private Set<Set<AlgorithmNode>> _uniqueProcessed;//####[53]####
//####[55]####
    boolean visualisation = true;//####[55]####
//####[57]####
    public AlgorithmImp(int numberOfCores) {//####[57]####
        _dag = DAGImp.getInstance();//####[58]####
        _numberOfCores = numberOfCores;//####[59]####
        _currentBestSchedule = new HashMap<String, NodeSchedule>();//####[60]####
        if (visualisation) //####[62]####
        {//####[62]####
            _model = TableModel.getInstance();//####[64]####
            _model.initModel(_currentBestSchedule, _dag, _numberOfCores);//####[65]####
            _chartModel = new BarChartModel();//####[67]####
            ComboView schedule = new ComboView(_model, _dag, _numberOfCores, _chartModel);//####[69]####
        }//####[70]####
        _uniqueProcessed = Collections.synchronizedSet(new HashSet<Set<AlgorithmNode>>());//####[72]####
        produceSequentialSchedule();//####[74]####
        produceGreedySchedule();//####[75]####
        _threads = new Semaphore(_numberOfThreads);//####[77]####
        Schedule emptySchedule = new ScheduleImp(_numberOfCores);//####[80]####
        recursiveScheduleGeneration(new ArrayList<AlgorithmNode>(), AlgorithmNode.convertNodetoAlgorithmNode(_dag.getAllNodes()), emptySchedule);//####[81]####
        try {//####[83]####
            _threads.acquire(_numberOfThreads);//####[84]####
        } catch (InterruptedException ex) {//####[85]####
            ex.printStackTrace();//####[86]####
        }//####[87]####
        if (visualisation) //####[89]####
        {//####[89]####
            _model.changeData(_currentBestSchedule, _bestTime);//####[90]####
            _model = TableModel.setInstance();//####[92]####
        }//####[93]####
    }//####[94]####
//####[99]####
    /**
	 * helper method for firing update.
	 *///####[99]####
    private void fireUpdateToGUI() {//####[99]####
        _chartModel.addDataToSeries(_bestTime);//####[104]####
        int timeNow = Clock.getInstance().getMilliseconds();//####[105]####
        Clock.lastUpdate = timeNow;//####[107]####
        _model.changeData(_currentBestSchedule, _bestTime);//####[108]####
    }//####[109]####
//####[117]####
    /**
	 * This method will produce a sequential schedule to set the lower bound.
	 * 
	 * This will be used together will the greedy schedule to bound
	 * the DFS.
	 *///####[117]####
    private void produceSequentialSchedule() {//####[117]####
        List<Node> reachableNodes = new ArrayList<Node>();//####[118]####
        List<Node> completedNodes = new ArrayList<Node>();//####[119]####
        List<Node> remainingNodes = new ArrayList<Node>();//####[120]####
        reachableNodes.addAll(_dag.getStartNodes());//####[122]####
        remainingNodes.addAll(_dag.getAllNodes());//####[123]####
        Schedule schedule = new ScheduleImp(_numberOfCores);//####[125]####
        while (!reachableNodes.isEmpty()) //####[127]####
        {//####[127]####
            Node toBeScheduled = reachableNodes.get(0);//####[128]####
            AlgorithmNode algNode = new AlgorithmNodeImp(toBeScheduled.getName());//####[130]####
            algNode.setCore(1);//####[131]####
            schedule = schedule.getNextSchedule(algNode);//####[132]####
            completedNodes.add(toBeScheduled);//####[135]####
            reachableNodes.remove(toBeScheduled);//####[136]####
            remainingNodes.remove(toBeScheduled);//####[137]####
            for (Node rn : remainingNodes) //####[138]####
            {//####[138]####
                if (completedNodes.containsAll(rn.getPredecessors()) && !reachableNodes.contains(rn)) //####[139]####
                {//####[139]####
                    reachableNodes.add(rn);//####[140]####
                }//####[141]####
            }//####[142]####
        }//####[143]####
        setNewBestSchedule(schedule);//####[146]####
        _bestTime = schedule.getTotalTime();//####[147]####
        if (visualisation) //####[149]####
        {//####[149]####
            fireUpdateToGUI();//####[150]####
        }//####[151]####
    }//####[152]####
//####[160]####
    /**
	 * This method will produce a greedy schedule to set the lower bound.
	 * 
	 * This will be used together will the sequential schedule to bound
	 * the DFS.
	 *///####[160]####
    private void produceGreedySchedule() {//####[160]####
        List<Node> reachableNodes = new ArrayList<Node>();//####[161]####
        List<Node> completedNodes = new ArrayList<Node>();//####[162]####
        List<Node> remainingNodes = new ArrayList<Node>();//####[163]####
        reachableNodes.addAll(_dag.getStartNodes());//####[165]####
        remainingNodes.addAll(_dag.getAllNodes());//####[166]####
        Schedule schedule = new ScheduleImp(_numberOfCores);//####[168]####
        while (!reachableNodes.isEmpty()) //####[170]####
        {//####[170]####
            List<Integer> reachableAmount = new ArrayList<Integer>();//####[172]####
            for (Node n : reachableNodes) //####[173]####
            {//####[173]####
                reachableAmount.add(n.getSuccessors().size());//####[174]####
            }//####[175]####
            int maxIndex = reachableAmount.indexOf(Collections.max(reachableAmount));//####[176]####
            Node toBeScheduled = reachableNodes.get(maxIndex);//####[177]####
            List<Integer> earliestStartTimes = new ArrayList<Integer>();//####[180]####
            for (int i = 1; i <= _numberOfCores; i++) //####[181]####
            {//####[181]####
                int coreStart = schedule.getFinishTimeForCore(i);//####[182]####
                AlgorithmNode algNode = new AlgorithmNodeImp(toBeScheduled.getName());//####[183]####
                algNode.setCore(i);//####[184]####
                int depStart = schedule.getDependencyBasedStartTime(toBeScheduled, algNode);//####[185]####
                earliestStartTimes.add((coreStart > depStart) ? coreStart : depStart);//####[186]####
            }//####[187]####
            int earliestCoreNo = earliestStartTimes.indexOf(Collections.min(earliestStartTimes)) + 1;//####[188]####
            AlgorithmNode algNode = new AlgorithmNodeImp(toBeScheduled.getName());//####[190]####
            algNode.setCore(earliestCoreNo);//####[191]####
            schedule = schedule.getNextSchedule(algNode);//####[192]####
            completedNodes.add(toBeScheduled);//####[195]####
            reachableNodes.remove(toBeScheduled);//####[196]####
            remainingNodes.remove(toBeScheduled);//####[197]####
            for (Node rn : remainingNodes) //####[198]####
            {//####[198]####
                if (completedNodes.containsAll(rn.getPredecessors()) && !reachableNodes.contains(rn)) //####[199]####
                {//####[199]####
                    reachableNodes.add(rn);//####[200]####
                }//####[201]####
            }//####[202]####
        }//####[203]####
        if (schedule.getTotalTime() < _bestTime) //####[205]####
        {//####[205]####
            setNewBestSchedule(schedule);//####[206]####
            _bestTime = schedule.getTotalTime();//####[207]####
            if (visualisation) //####[209]####
            {//####[209]####
                fireUpdateToGUI();//####[210]####
            }//####[211]####
        }//####[212]####
    }//####[213]####
//####[220]####
    /**
	 * Purely for benchmarking purposes
	 *
	 * @return number of times the recursive method was called
	 *///####[220]####
    public int getRecursiveCalls() {//####[220]####
        return _recursiveCalls;//####[221]####
    }//####[222]####
//####[224]####
    private synchronized void compareSchedules(Schedule s) {//####[224]####
        if (s.getTotalTime() < _bestTime) //####[225]####
        {//####[225]####
            setNewBestSchedule(s);//####[226]####
            _bestTime = s.getTotalTime();//####[227]####
            if (visualisation) //####[229]####
            {//####[229]####
                fireUpdateToGUI();//####[230]####
            }//####[231]####
        }//####[232]####
    }//####[233]####
//####[235]####
    private static volatile Method __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method = null;//####[235]####
    private synchronized static void __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet() {//####[235]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[235]####
            try {//####[235]####
                __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method = ParaTaskHelper.getDeclaredMethod(new ParaTaskHelper.ClassGetter().getCurrentClass(), "__pt__recursiveScheduleGenerationTask", new Class[] {//####[235]####
                    List.class, List.class, Schedule.class//####[235]####
                });//####[235]####
            } catch (Exception e) {//####[235]####
                e.printStackTrace();//####[235]####
            }//####[235]####
        }//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, Schedule prev) {//####[235]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[235]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[235]####
        // ensure Method variable is set//####[235]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[235]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[235]####
        }//####[235]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[235]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[235]####
        taskinfo.setInstance(this);//####[235]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, Schedule prev) {//####[235]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[235]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[235]####
        // ensure Method variable is set//####[235]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[235]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[235]####
        }//####[235]####
        taskinfo.setTaskIdArgIndexes(0);//####[235]####
        taskinfo.addDependsOn(processed);//####[235]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[235]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[235]####
        taskinfo.setInstance(this);//####[235]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, Schedule prev) {//####[235]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[235]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[235]####
        // ensure Method variable is set//####[235]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[235]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[235]####
        }//####[235]####
        taskinfo.setQueueArgIndexes(0);//####[235]####
        taskinfo.setIsPipeline(true);//####[235]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[235]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[235]####
        taskinfo.setInstance(this);//####[235]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, TaskID<List<AlgorithmNode>> remainingNodes, Schedule prev) {//####[235]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[235]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, TaskID<List<AlgorithmNode>> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[235]####
        // ensure Method variable is set//####[235]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[235]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[235]####
        }//####[235]####
        taskinfo.setTaskIdArgIndexes(1);//####[235]####
        taskinfo.addDependsOn(remainingNodes);//####[235]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[235]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[235]####
        taskinfo.setInstance(this);//####[235]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, Schedule prev) {//####[235]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[235]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[235]####
        // ensure Method variable is set//####[235]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[235]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[235]####
        }//####[235]####
        taskinfo.setTaskIdArgIndexes(0, 1);//####[235]####
        taskinfo.addDependsOn(processed);//####[235]####
        taskinfo.addDependsOn(remainingNodes);//####[235]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[235]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[235]####
        taskinfo.setInstance(this);//####[235]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, Schedule prev) {//####[235]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[235]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[235]####
        // ensure Method variable is set//####[235]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[235]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[235]####
        }//####[235]####
        taskinfo.setQueueArgIndexes(0);//####[235]####
        taskinfo.setIsPipeline(true);//####[235]####
        taskinfo.setTaskIdArgIndexes(1);//####[235]####
        taskinfo.addDependsOn(remainingNodes);//####[235]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[235]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[235]####
        taskinfo.setInstance(this);//####[235]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, Schedule prev) {//####[235]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[235]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[235]####
        // ensure Method variable is set//####[235]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[235]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[235]####
        }//####[235]####
        taskinfo.setQueueArgIndexes(1);//####[235]####
        taskinfo.setIsPipeline(true);//####[235]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[235]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[235]####
        taskinfo.setInstance(this);//####[235]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, Schedule prev) {//####[235]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[235]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[235]####
        // ensure Method variable is set//####[235]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[235]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[235]####
        }//####[235]####
        taskinfo.setQueueArgIndexes(1);//####[235]####
        taskinfo.setIsPipeline(true);//####[235]####
        taskinfo.setTaskIdArgIndexes(0);//####[235]####
        taskinfo.addDependsOn(processed);//####[235]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[235]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[235]####
        taskinfo.setInstance(this);//####[235]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, Schedule prev) {//####[235]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[235]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[235]####
        // ensure Method variable is set//####[235]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[235]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[235]####
        }//####[235]####
        taskinfo.setQueueArgIndexes(0, 1);//####[235]####
        taskinfo.setIsPipeline(true);//####[235]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[235]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[235]####
        taskinfo.setInstance(this);//####[235]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, TaskID<Schedule> prev) {//####[235]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[235]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[235]####
        // ensure Method variable is set//####[235]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[235]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[235]####
        }//####[235]####
        taskinfo.setTaskIdArgIndexes(2);//####[235]####
        taskinfo.addDependsOn(prev);//####[235]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[235]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[235]####
        taskinfo.setInstance(this);//####[235]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, TaskID<Schedule> prev) {//####[235]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[235]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[235]####
        // ensure Method variable is set//####[235]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[235]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[235]####
        }//####[235]####
        taskinfo.setTaskIdArgIndexes(0, 2);//####[235]####
        taskinfo.addDependsOn(processed);//####[235]####
        taskinfo.addDependsOn(prev);//####[235]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[235]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[235]####
        taskinfo.setInstance(this);//####[235]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, TaskID<Schedule> prev) {//####[235]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[235]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[235]####
        // ensure Method variable is set//####[235]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[235]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[235]####
        }//####[235]####
        taskinfo.setQueueArgIndexes(0);//####[235]####
        taskinfo.setIsPipeline(true);//####[235]####
        taskinfo.setTaskIdArgIndexes(2);//####[235]####
        taskinfo.addDependsOn(prev);//####[235]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[235]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[235]####
        taskinfo.setInstance(this);//####[235]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, TaskID<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev) {//####[235]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[235]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, TaskID<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[235]####
        // ensure Method variable is set//####[235]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[235]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[235]####
        }//####[235]####
        taskinfo.setTaskIdArgIndexes(1, 2);//####[235]####
        taskinfo.addDependsOn(remainingNodes);//####[235]####
        taskinfo.addDependsOn(prev);//####[235]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[235]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[235]####
        taskinfo.setInstance(this);//####[235]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev) {//####[235]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[235]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[235]####
        // ensure Method variable is set//####[235]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[235]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[235]####
        }//####[235]####
        taskinfo.setTaskIdArgIndexes(0, 1, 2);//####[235]####
        taskinfo.addDependsOn(processed);//####[235]####
        taskinfo.addDependsOn(remainingNodes);//####[235]####
        taskinfo.addDependsOn(prev);//####[235]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[235]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[235]####
        taskinfo.setInstance(this);//####[235]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev) {//####[235]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[235]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[235]####
        // ensure Method variable is set//####[235]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[235]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[235]####
        }//####[235]####
        taskinfo.setQueueArgIndexes(0);//####[235]####
        taskinfo.setIsPipeline(true);//####[235]####
        taskinfo.setTaskIdArgIndexes(1, 2);//####[235]####
        taskinfo.addDependsOn(remainingNodes);//####[235]####
        taskinfo.addDependsOn(prev);//####[235]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[235]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[235]####
        taskinfo.setInstance(this);//####[235]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev) {//####[235]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[235]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[235]####
        // ensure Method variable is set//####[235]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[235]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[235]####
        }//####[235]####
        taskinfo.setQueueArgIndexes(1);//####[235]####
        taskinfo.setIsPipeline(true);//####[235]####
        taskinfo.setTaskIdArgIndexes(2);//####[235]####
        taskinfo.addDependsOn(prev);//####[235]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[235]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[235]####
        taskinfo.setInstance(this);//####[235]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev) {//####[235]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[235]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[235]####
        // ensure Method variable is set//####[235]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[235]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[235]####
        }//####[235]####
        taskinfo.setQueueArgIndexes(1);//####[235]####
        taskinfo.setIsPipeline(true);//####[235]####
        taskinfo.setTaskIdArgIndexes(0, 2);//####[235]####
        taskinfo.addDependsOn(processed);//####[235]####
        taskinfo.addDependsOn(prev);//####[235]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[235]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[235]####
        taskinfo.setInstance(this);//####[235]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev) {//####[235]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[235]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[235]####
        // ensure Method variable is set//####[235]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[235]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[235]####
        }//####[235]####
        taskinfo.setQueueArgIndexes(0, 1);//####[235]####
        taskinfo.setIsPipeline(true);//####[235]####
        taskinfo.setTaskIdArgIndexes(2);//####[235]####
        taskinfo.addDependsOn(prev);//####[235]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[235]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[235]####
        taskinfo.setInstance(this);//####[235]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, BlockingQueue<Schedule> prev) {//####[235]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[235]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[235]####
        // ensure Method variable is set//####[235]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[235]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[235]####
        }//####[235]####
        taskinfo.setQueueArgIndexes(2);//####[235]####
        taskinfo.setIsPipeline(true);//####[235]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[235]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[235]####
        taskinfo.setInstance(this);//####[235]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, BlockingQueue<Schedule> prev) {//####[235]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[235]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[235]####
        // ensure Method variable is set//####[235]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[235]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[235]####
        }//####[235]####
        taskinfo.setQueueArgIndexes(2);//####[235]####
        taskinfo.setIsPipeline(true);//####[235]####
        taskinfo.setTaskIdArgIndexes(0);//####[235]####
        taskinfo.addDependsOn(processed);//####[235]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[235]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[235]####
        taskinfo.setInstance(this);//####[235]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, BlockingQueue<Schedule> prev) {//####[235]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[235]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[235]####
        // ensure Method variable is set//####[235]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[235]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[235]####
        }//####[235]####
        taskinfo.setQueueArgIndexes(0, 2);//####[235]####
        taskinfo.setIsPipeline(true);//####[235]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[235]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[235]####
        taskinfo.setInstance(this);//####[235]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, TaskID<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev) {//####[235]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[235]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, TaskID<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[235]####
        // ensure Method variable is set//####[235]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[235]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[235]####
        }//####[235]####
        taskinfo.setQueueArgIndexes(2);//####[235]####
        taskinfo.setIsPipeline(true);//####[235]####
        taskinfo.setTaskIdArgIndexes(1);//####[235]####
        taskinfo.addDependsOn(remainingNodes);//####[235]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[235]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[235]####
        taskinfo.setInstance(this);//####[235]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev) {//####[235]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[235]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[235]####
        // ensure Method variable is set//####[235]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[235]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[235]####
        }//####[235]####
        taskinfo.setQueueArgIndexes(2);//####[235]####
        taskinfo.setIsPipeline(true);//####[235]####
        taskinfo.setTaskIdArgIndexes(0, 1);//####[235]####
        taskinfo.addDependsOn(processed);//####[235]####
        taskinfo.addDependsOn(remainingNodes);//####[235]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[235]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[235]####
        taskinfo.setInstance(this);//####[235]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev) {//####[235]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[235]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[235]####
        // ensure Method variable is set//####[235]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[235]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[235]####
        }//####[235]####
        taskinfo.setQueueArgIndexes(0, 2);//####[235]####
        taskinfo.setIsPipeline(true);//####[235]####
        taskinfo.setTaskIdArgIndexes(1);//####[235]####
        taskinfo.addDependsOn(remainingNodes);//####[235]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[235]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[235]####
        taskinfo.setInstance(this);//####[235]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev) {//####[235]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[235]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[235]####
        // ensure Method variable is set//####[235]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[235]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[235]####
        }//####[235]####
        taskinfo.setQueueArgIndexes(1, 2);//####[235]####
        taskinfo.setIsPipeline(true);//####[235]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[235]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[235]####
        taskinfo.setInstance(this);//####[235]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev) {//####[235]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[235]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[235]####
        // ensure Method variable is set//####[235]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[235]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[235]####
        }//####[235]####
        taskinfo.setQueueArgIndexes(1, 2);//####[235]####
        taskinfo.setIsPipeline(true);//####[235]####
        taskinfo.setTaskIdArgIndexes(0);//####[235]####
        taskinfo.addDependsOn(processed);//####[235]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[235]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[235]####
        taskinfo.setInstance(this);//####[235]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev) {//####[235]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[235]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[235]####
    }//####[235]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[235]####
        // ensure Method variable is set//####[235]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[235]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[235]####
        }//####[235]####
        taskinfo.setQueueArgIndexes(0, 1, 2);//####[235]####
        taskinfo.setIsPipeline(true);//####[235]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[235]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[235]####
        taskinfo.setInstance(this);//####[235]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[235]####
    }//####[235]####
    public void __pt__recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, Schedule prev) {//####[235]####
        recursiveScheduleGeneration(processed, remainingNodes, prev);//####[236]####
        _threads.release();//####[237]####
    }//####[238]####
//####[238]####
//####[252]####
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
	 *///####[252]####
    private void recursiveScheduleGeneration(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, Schedule prev) {//####[252]####
        _recursiveCalls++;//####[253]####
        if (remainingNodes.size() == 0) //####[256]####
        {//####[256]####
            Schedule finalSchedule = prev;//####[257]####
            compareSchedules(finalSchedule);//####[260]####
        } else {//####[261]####
            for (int i = 0; i < remainingNodes.size(); i++) //####[262]####
            {//####[262]####
                Schedule newSchedule;//####[263]####
                for (int j = 1; j <= _numberOfCores; j++) //####[266]####
                {//####[266]####
                    List<AlgorithmNode> newProcessed = new ArrayList<AlgorithmNode>(processed);//####[270]####
                    AlgorithmNode node = remainingNodes.get(i).createClone();//####[271]####
                    node.setCore(j);//####[272]####
                    newProcessed.add(node);//####[273]####
                    Set<AlgorithmNode> algNodesSet = new HashSet<AlgorithmNode>(newProcessed);//####[275]####
                    if (checkValidSchedule(newProcessed)) //####[277]####
                    {//####[277]####
                        newSchedule = prev.getNextSchedule(node);//####[278]####
                        if ((newSchedule.getTotalTime() >= _bestTime)) //####[281]####
                        {//####[281]####
                            continue;//####[282]####
                        }//####[283]####
                    } else {//####[284]####
                        break;//####[285]####
                    }//####[286]####
                    if (_uniqueProcessed.contains(algNodesSet)) //####[297]####
                    {//####[297]####
                        continue;//####[298]####
                    } else {//####[300]####
                        _uniqueProcessed.add(algNodesSet);//####[301]####
                    }//####[302]####
                    List<AlgorithmNode> newRemaining = new ArrayList<AlgorithmNode>(remainingNodes);//####[306]####
                    newRemaining.remove(i);//####[307]####
                    if (_dag.getNodeByName(node.getNodeName()).getSuccessors().size() > 1 && _threads.tryAcquire()) //####[309]####
                    {//####[309]####
                        recursiveScheduleGenerationTask(newProcessed, newRemaining, newSchedule);//####[310]####
                    } else {//####[311]####
                        recursiveScheduleGeneration(newProcessed, newRemaining, newSchedule);//####[312]####
                    }//####[313]####
                    List<Integer> coresAssigned = new ArrayList<Integer>();//####[328]####
                    for (AlgorithmNode algNode : processed) //####[329]####
                    {//####[329]####
                        coresAssigned.add(algNode.getCore());//####[330]####
                    }//####[331]####
                    if (!coresAssigned.contains(node.getCore())) //####[333]####
                    {//####[333]####
                        break;//####[334]####
                    }//####[335]####
                }//####[336]####
            }//####[337]####
        }//####[338]####
    }//####[339]####
//####[341]####
    private void setNewBestSchedule(Schedule finalSchedule) {//####[341]####
        for (int i = 0; i < finalSchedule.getSizeOfSchedule(); i++) //####[342]####
        {//####[342]####
            NodeSchedule nodeSchedule = new NodeScheduleImp(finalSchedule.getNodeStartTime(i), finalSchedule.getNodeCore(i));//####[343]####
            _currentBestSchedule.put(finalSchedule.getNodeName(i), nodeSchedule);//####[344]####
        }//####[347]####
    }//####[348]####
//####[357]####
    /**
	 * This method determines whether a schedule is valid. It does this by ensuring a nodes predecessors are scheduled
	 * before the current node
	 *
	 * @param schedule
	 * @return true if the schedule is valid, false if not
	 *///####[357]####
    private boolean checkValidSchedule(List<AlgorithmNode> schedule) {//####[357]####
        if (schedule == null) //####[358]####
        {//####[358]####
            return false;//####[359]####
        }//####[360]####
        Node currentNode = _dag.getNodeByName(schedule.get(schedule.size() - 1).getNodeName());//####[363]####
        List<Node> predecessors = currentNode.getPredecessors();//####[364]####
        if (predecessors.size() == 0) //####[367]####
        {//####[367]####
            return true;//####[368]####
        } else if (schedule.size() == 1) //####[369]####
        {//####[369]####
            return false;//####[370]####
        }//####[371]####
        int counter = 0;//####[374]####
        for (int i = schedule.size() - 2; i >= 0; i--) //####[375]####
        {//####[375]####
            for (Node preNode : predecessors) //####[376]####
            {//####[376]####
                if (schedule.get(i).getNodeName().equals(preNode.getName())) //####[377]####
                {//####[377]####
                    counter++;//####[378]####
                    break;//####[379]####
                }//####[380]####
            }//####[381]####
        }//####[382]####
        if (counter != predecessors.size()) //####[385]####
        {//####[385]####
            return false;//####[386]####
        }//####[387]####
        return true;//####[388]####
    }//####[389]####
//####[397]####
    /**
	 * Calculates the time cost of executing the given schedule, returning a complete ScheduleImp object.
	 * @param algNodes - A {@code List<AlgorithmNode>} given in the order of execution
	 * @return - ScheduleImp object with cost and execution time information
	 *///####[397]####
    @Deprecated//####[397]####
    private ScheduleImp calculateTotalTime(List<AlgorithmNode> algNodes) {//####[397]####
        List<Node> nodes = new ArrayList<Node>();//####[399]####
        for (AlgorithmNode algNode : algNodes) //####[402]####
        {//####[402]####
            nodes.add(_dag.getNodeByName(algNode.getNodeName()));//####[403]####
        }//####[404]####
        List<AlgorithmNode> latestAlgNodeInSchedules = Arrays.asList(new AlgorithmNode[_numberOfCores]);//####[408]####
        ScheduleImp st = new ScheduleImp(algNodes, _numberOfCores);//####[411]####
        for (AlgorithmNode currentAlgNode : algNodes) //####[414]####
        {//####[414]####
            Node currentNode = nodes.get(algNodes.indexOf(currentAlgNode));//####[415]####
            int highestCost = 0;//####[416]####
            for (Node node : currentNode.getPredecessors()) //####[419]####
            {//####[419]####
                int cost = st.getNodeStartTime(getIndexOfList(node, algNodes)) + node.getWeight();//####[421]####
                if (!(algNodes.get(getIndexOfList(node, algNodes)).getCore() == currentAlgNode.getCore())) //####[422]####
                {//####[422]####
                    cost += currentNode.getInArc(node).getWeight();//####[424]####
                }//####[425]####
                if (cost > highestCost) //####[427]####
                {//####[427]####
                    highestCost = cost;//####[428]####
                }//####[429]####
            }//####[430]####
            AlgorithmNode latestNode = latestAlgNodeInSchedules.get(currentAlgNode.getCore() - 1);//####[433]####
            if (latestNode != null) //####[434]####
            {//####[434]####
                Node previousNode = _dag.getNodeByName(latestNode.getNodeName());//####[435]####
                int cost = previousNode.getWeight() + st.getNodeStartTime(algNodes.indexOf(latestNode));//####[436]####
                if (cost > highestCost) //####[437]####
                {//####[437]####
                    highestCost = cost;//####[438]####
                }//####[439]####
            }//####[440]####
            latestAlgNodeInSchedules.set(currentAlgNode.getCore() - 1, currentAlgNode);//####[443]####
            st.setStartTimeForNode(highestCost, algNodes.indexOf(currentAlgNode));//####[446]####
        }//####[447]####
        setTimeForSchedule(latestAlgNodeInSchedules, algNodes, st);//####[449]####
        return st;//####[451]####
    }//####[452]####
//####[462]####
    /**
	 * Calculates and sets the total time in the {@code ScheduleImp} object given.
	 * Main purpose is to make the code more readable.
	 * @param latestAlgNodeInSchedules - {@code List<AlgorithmNode>} containing the last node in each processor
	 * @param algNodes - the same {@code List<AlgorithmnNode>} used to construct the {@code ScheduleImp} object
	 * @param st - {@code ScheduleImp} object to set the total time of
	 *///####[462]####
    @Deprecated//####[462]####
    private void setTimeForSchedule(List<AlgorithmNode> latestAlgNodeInSchedules, List<AlgorithmNode> algNodes, ScheduleImp st) {//####[462]####
        int totalTime = 0;//####[463]####
        for (int i = 1; i <= _numberOfCores; i++) //####[464]####
        {//####[464]####
            AlgorithmNode latestAlgNode = latestAlgNodeInSchedules.get(i - 1);//####[465]####
            int timeTaken = 0;//####[467]####
            if (latestAlgNode != null) //####[468]####
            {//####[468]####
                timeTaken = st.getNodeStartTime(algNodes.indexOf(latestAlgNode)) + _dag.getNodeByName(latestAlgNode.getNodeName()).getWeight();//####[469]####
            }//####[470]####
            if (timeTaken > totalTime) //####[472]####
            {//####[472]####
                totalTime = timeTaken;//####[473]####
            }//####[474]####
        }//####[475]####
        st.setTotalTime(totalTime);//####[477]####
    }//####[478]####
//####[487]####
    /**
	 * Finds and returns the index position of the corresponding {@code AlgorithmNode} within the given {@code List<AlgorithmNode}
	 * @param node - {@code Node} to find the corresponding index position for
	 * @param algNodes - {@code List<AlgorithmNode>} to find the index for
	 * @return the index position of the corresponding {@code AlgorithmNode} object
	 *///####[487]####
    @Deprecated//####[487]####
    private int getIndexOfList(Node node, List<AlgorithmNode> algNodes) {//####[487]####
        for (AlgorithmNode algNode : algNodes) //####[488]####
        {//####[488]####
            if (node.getName().equals(algNode.getNodeName())) //####[489]####
            {//####[489]####
                return algNodes.indexOf(algNode);//####[490]####
            }//####[491]####
        }//####[492]####
        return -1;//####[493]####
    }//####[494]####
//####[497]####
    @Override//####[497]####
    public HashMap<String, NodeSchedule> getCurrentBestSchedule() {//####[497]####
        return _currentBestSchedule;//####[498]####
    }//####[499]####
//####[502]####
    @Override//####[502]####
    public int getBestTotalTime() {//####[502]####
        return _bestTime;//####[503]####
    }//####[504]####
//####[512]####
    /**
	 * The wrapper methods purely for testing. (as the methods were declared to be private)
	 * @param algNodes
	 * @return
	 *///####[512]####
    @Deprecated//####[512]####
    public ScheduleImp calculateTotalTimeWrapper(List<AlgorithmNode> algNodes) {//####[512]####
        return calculateTotalTime(algNodes);//####[513]####
    }//####[514]####
//####[516]####
    public boolean checkValidScheduleWrapper(List<AlgorithmNode> s1) {//####[516]####
        return checkValidSchedule(s1);//####[517]####
    }//####[518]####
}//####[518]####
