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
    private final int _numberOfThreads = 16;//####[48]####
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
    private ArrayList<Integer> _threadCount;//####[59]####
//####[61]####
    public AlgorithmImp(int numberOfCores) {//####[61]####
        _threadCount = new ArrayList<Integer>();//####[62]####
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
        try {//####[85]####
            _threads.acquire();//####[86]####
        } catch (InterruptedException ex) {//####[87]####
            ex.printStackTrace();//####[88]####
        }//####[89]####
        Schedule emptySchedule = new ScheduleImp(_numberOfCores);//####[91]####
        recursiveScheduleGenerationTask(new ArrayList<AlgorithmNode>(), AlgorithmNode.convertNodetoAlgorithmNode(_dag.getAllNodes()), emptySchedule);//####[92]####
        try {//####[94]####
            while (true) //####[95]####
            {//####[95]####
                if (_threads.tryAcquire(_numberOfThreads)) //####[96]####
                {//####[96]####
                    break;//####[97]####
                }//####[98]####
                Thread.sleep(1000L);//####[99]####
                addToThreadCount();//####[100]####
            }//####[101]####
        } catch (InterruptedException ex) {//####[103]####
            ex.printStackTrace();//####[104]####
        }//####[105]####
        if (visualisation) //####[107]####
        {//####[107]####
            _model.changeData(_currentBestSchedule, _bestTime);//####[108]####
            _model = TableModel.setInstance();//####[110]####
        }//####[111]####
        printThreadCount();//####[113]####
    }//####[114]####
//####[116]####
    private synchronized void addToThreadCount() {//####[116]####
        _threadCount.add(_numberOfThreads - _threads.availablePermits());//####[117]####
    }//####[118]####
//####[120]####
    private void printThreadCount() {//####[120]####
        ArrayList<Integer> total = new ArrayList<Integer>();//####[121]####
        for (int i = 0; i < _numberOfThreads + 1; i++) //####[122]####
        {//####[122]####
            total.add(0);//####[123]####
        }//####[124]####
        for (Integer i : _threadCount) //####[126]####
        {//####[126]####
            total.set(i, total.get(i) + 1);//####[127]####
        }//####[128]####
        for (int i = 0; i < _numberOfThreads + 1; i++) //####[130]####
        {//####[130]####
            System.out.println("Thread count: " + i + "Percentage: " + total.get(i).doubleValue() / _threadCount.size());//####[131]####
        }//####[132]####
    }//####[133]####
//####[138]####
    /**
	 * helper method for firing update.
	 *///####[138]####
    private void fireUpdateToGUI() {//####[138]####
        _chartModel.addDataToSeries(_bestTime);//####[143]####
        int timeNow = Clock.getInstance().getMilliseconds();//####[144]####
        Clock.lastUpdate = timeNow;//####[146]####
        _model.changeData(_currentBestSchedule, _bestTime);//####[147]####
    }//####[148]####
//####[156]####
    /**
	 * This method will produce a sequential schedule to set the lower bound.
	 * 
	 * This will be used together will the greedy schedule to bound
	 * the DFS.
	 *///####[156]####
    private void produceSequentialSchedule() {//####[156]####
        List<Node> reachableNodes = new ArrayList<Node>();//####[157]####
        List<Node> completedNodes = new ArrayList<Node>();//####[158]####
        List<Node> remainingNodes = new ArrayList<Node>();//####[159]####
        reachableNodes.addAll(_dag.getStartNodes());//####[161]####
        remainingNodes.addAll(_dag.getAllNodes());//####[162]####
        Schedule schedule = new ScheduleImp(_numberOfCores);//####[164]####
        while (!reachableNodes.isEmpty()) //####[166]####
        {//####[166]####
            Node toBeScheduled = reachableNodes.get(0);//####[167]####
            AlgorithmNode algNode = new AlgorithmNodeImp(toBeScheduled.getName());//####[169]####
            algNode.setCore(1);//####[170]####
            schedule = schedule.getNextSchedule(algNode);//####[171]####
            completedNodes.add(toBeScheduled);//####[174]####
            reachableNodes.remove(toBeScheduled);//####[175]####
            remainingNodes.remove(toBeScheduled);//####[176]####
            for (Node rn : remainingNodes) //####[177]####
            {//####[177]####
                if (completedNodes.containsAll(rn.getPredecessors()) && !reachableNodes.contains(rn)) //####[178]####
                {//####[178]####
                    reachableNodes.add(rn);//####[179]####
                }//####[180]####
            }//####[181]####
        }//####[182]####
        setNewBestSchedule(schedule);//####[185]####
        _bestTime = schedule.getTotalTime();//####[186]####
        if (visualisation) //####[188]####
        {//####[188]####
            fireUpdateToGUI();//####[189]####
        }//####[190]####
    }//####[191]####
//####[199]####
    /**
	 * This method will produce a greedy schedule to set the lower bound.
	 * 
	 * This will be used together will the sequential schedule to bound
	 * the DFS.
	 *///####[199]####
    private void produceGreedySchedule() {//####[199]####
        List<Node> reachableNodes = new ArrayList<Node>();//####[200]####
        List<Node> completedNodes = new ArrayList<Node>();//####[201]####
        List<Node> remainingNodes = new ArrayList<Node>();//####[202]####
        reachableNodes.addAll(_dag.getStartNodes());//####[204]####
        remainingNodes.addAll(_dag.getAllNodes());//####[205]####
        Schedule schedule = new ScheduleImp(_numberOfCores);//####[207]####
        while (!reachableNodes.isEmpty()) //####[209]####
        {//####[209]####
            List<Integer> reachableAmount = new ArrayList<Integer>();//####[211]####
            for (Node n : reachableNodes) //####[212]####
            {//####[212]####
                reachableAmount.add(n.getSuccessors().size());//####[213]####
            }//####[214]####
            int maxIndex = reachableAmount.indexOf(Collections.max(reachableAmount));//####[215]####
            Node toBeScheduled = reachableNodes.get(maxIndex);//####[216]####
            List<Integer> earliestStartTimes = new ArrayList<Integer>();//####[219]####
            for (int i = 1; i <= _numberOfCores; i++) //####[220]####
            {//####[220]####
                int coreStart = schedule.getFinishTimeForCore(i);//####[221]####
                AlgorithmNode algNode = new AlgorithmNodeImp(toBeScheduled.getName());//####[222]####
                algNode.setCore(i);//####[223]####
                int depStart = schedule.getDependencyBasedStartTime(toBeScheduled, algNode);//####[224]####
                earliestStartTimes.add((coreStart > depStart) ? coreStart : depStart);//####[225]####
            }//####[226]####
            int earliestCoreNo = earliestStartTimes.indexOf(Collections.min(earliestStartTimes)) + 1;//####[227]####
            AlgorithmNode algNode = new AlgorithmNodeImp(toBeScheduled.getName());//####[229]####
            algNode.setCore(earliestCoreNo);//####[230]####
            schedule = schedule.getNextSchedule(algNode);//####[231]####
            completedNodes.add(toBeScheduled);//####[234]####
            reachableNodes.remove(toBeScheduled);//####[235]####
            remainingNodes.remove(toBeScheduled);//####[236]####
            for (Node rn : remainingNodes) //####[237]####
            {//####[237]####
                if (completedNodes.containsAll(rn.getPredecessors()) && !reachableNodes.contains(rn)) //####[238]####
                {//####[238]####
                    reachableNodes.add(rn);//####[239]####
                }//####[240]####
            }//####[241]####
        }//####[242]####
        if (schedule.getTotalTime() < _bestTime) //####[244]####
        {//####[244]####
            setNewBestSchedule(schedule);//####[245]####
            _bestTime = schedule.getTotalTime();//####[246]####
            if (visualisation) //####[248]####
            {//####[248]####
                fireUpdateToGUI();//####[249]####
            }//####[250]####
        }//####[251]####
    }//####[252]####
//####[259]####
    /**
	 * Purely for benchmarking purposes
	 *
	 * @return number of times the recursive method was called
	 *///####[259]####
    public int getRecursiveCalls() {//####[259]####
        return _recursiveCalls.get();//####[260]####
    }//####[261]####
//####[263]####
    private synchronized void compareSchedules(Schedule s) {//####[263]####
        if (s.getTotalTime() < _bestTime) //####[264]####
        {//####[264]####
            setNewBestSchedule(s);//####[265]####
            _bestTime = s.getTotalTime();//####[266]####
            if (visualisation) //####[268]####
            {//####[268]####
                fireUpdateToGUI();//####[269]####
            }//####[270]####
        }//####[271]####
    }//####[272]####
//####[274]####
    private static volatile Method __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method = null;//####[274]####
    private synchronized static void __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet() {//####[274]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[274]####
            try {//####[274]####
                __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method = ParaTaskHelper.getDeclaredMethod(new ParaTaskHelper.ClassGetter().getCurrentClass(), "__pt__recursiveScheduleGenerationTask", new Class[] {//####[274]####
                    List.class, List.class, Schedule.class//####[274]####
                });//####[274]####
            } catch (Exception e) {//####[274]####
                e.printStackTrace();//####[274]####
            }//####[274]####
        }//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, Schedule prev) {//####[274]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[274]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[274]####
        // ensure Method variable is set//####[274]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[274]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[274]####
        }//####[274]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[274]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[274]####
        taskinfo.setInstance(this);//####[274]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, Schedule prev) {//####[274]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[274]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[274]####
        // ensure Method variable is set//####[274]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[274]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[274]####
        }//####[274]####
        taskinfo.setTaskIdArgIndexes(0);//####[274]####
        taskinfo.addDependsOn(processed);//####[274]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[274]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[274]####
        taskinfo.setInstance(this);//####[274]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, Schedule prev) {//####[274]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[274]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[274]####
        // ensure Method variable is set//####[274]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[274]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[274]####
        }//####[274]####
        taskinfo.setQueueArgIndexes(0);//####[274]####
        taskinfo.setIsPipeline(true);//####[274]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[274]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[274]####
        taskinfo.setInstance(this);//####[274]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, TaskID<List<AlgorithmNode>> remainingNodes, Schedule prev) {//####[274]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[274]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, TaskID<List<AlgorithmNode>> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[274]####
        // ensure Method variable is set//####[274]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[274]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[274]####
        }//####[274]####
        taskinfo.setTaskIdArgIndexes(1);//####[274]####
        taskinfo.addDependsOn(remainingNodes);//####[274]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[274]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[274]####
        taskinfo.setInstance(this);//####[274]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, Schedule prev) {//####[274]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[274]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[274]####
        // ensure Method variable is set//####[274]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[274]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[274]####
        }//####[274]####
        taskinfo.setTaskIdArgIndexes(0, 1);//####[274]####
        taskinfo.addDependsOn(processed);//####[274]####
        taskinfo.addDependsOn(remainingNodes);//####[274]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[274]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[274]####
        taskinfo.setInstance(this);//####[274]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, Schedule prev) {//####[274]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[274]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[274]####
        // ensure Method variable is set//####[274]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[274]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[274]####
        }//####[274]####
        taskinfo.setQueueArgIndexes(0);//####[274]####
        taskinfo.setIsPipeline(true);//####[274]####
        taskinfo.setTaskIdArgIndexes(1);//####[274]####
        taskinfo.addDependsOn(remainingNodes);//####[274]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[274]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[274]####
        taskinfo.setInstance(this);//####[274]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, Schedule prev) {//####[274]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[274]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[274]####
        // ensure Method variable is set//####[274]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[274]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[274]####
        }//####[274]####
        taskinfo.setQueueArgIndexes(1);//####[274]####
        taskinfo.setIsPipeline(true);//####[274]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[274]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[274]####
        taskinfo.setInstance(this);//####[274]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, Schedule prev) {//####[274]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[274]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[274]####
        // ensure Method variable is set//####[274]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[274]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[274]####
        }//####[274]####
        taskinfo.setQueueArgIndexes(1);//####[274]####
        taskinfo.setIsPipeline(true);//####[274]####
        taskinfo.setTaskIdArgIndexes(0);//####[274]####
        taskinfo.addDependsOn(processed);//####[274]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[274]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[274]####
        taskinfo.setInstance(this);//####[274]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, Schedule prev) {//####[274]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[274]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[274]####
        // ensure Method variable is set//####[274]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[274]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[274]####
        }//####[274]####
        taskinfo.setQueueArgIndexes(0, 1);//####[274]####
        taskinfo.setIsPipeline(true);//####[274]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[274]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[274]####
        taskinfo.setInstance(this);//####[274]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, TaskID<Schedule> prev) {//####[274]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[274]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[274]####
        // ensure Method variable is set//####[274]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[274]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[274]####
        }//####[274]####
        taskinfo.setTaskIdArgIndexes(2);//####[274]####
        taskinfo.addDependsOn(prev);//####[274]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[274]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[274]####
        taskinfo.setInstance(this);//####[274]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, TaskID<Schedule> prev) {//####[274]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[274]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[274]####
        // ensure Method variable is set//####[274]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[274]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[274]####
        }//####[274]####
        taskinfo.setTaskIdArgIndexes(0, 2);//####[274]####
        taskinfo.addDependsOn(processed);//####[274]####
        taskinfo.addDependsOn(prev);//####[274]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[274]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[274]####
        taskinfo.setInstance(this);//####[274]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, TaskID<Schedule> prev) {//####[274]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[274]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[274]####
        // ensure Method variable is set//####[274]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[274]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[274]####
        }//####[274]####
        taskinfo.setQueueArgIndexes(0);//####[274]####
        taskinfo.setIsPipeline(true);//####[274]####
        taskinfo.setTaskIdArgIndexes(2);//####[274]####
        taskinfo.addDependsOn(prev);//####[274]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[274]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[274]####
        taskinfo.setInstance(this);//####[274]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, TaskID<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev) {//####[274]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[274]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, TaskID<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[274]####
        // ensure Method variable is set//####[274]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[274]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[274]####
        }//####[274]####
        taskinfo.setTaskIdArgIndexes(1, 2);//####[274]####
        taskinfo.addDependsOn(remainingNodes);//####[274]####
        taskinfo.addDependsOn(prev);//####[274]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[274]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[274]####
        taskinfo.setInstance(this);//####[274]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev) {//####[274]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[274]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[274]####
        // ensure Method variable is set//####[274]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[274]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[274]####
        }//####[274]####
        taskinfo.setTaskIdArgIndexes(0, 1, 2);//####[274]####
        taskinfo.addDependsOn(processed);//####[274]####
        taskinfo.addDependsOn(remainingNodes);//####[274]####
        taskinfo.addDependsOn(prev);//####[274]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[274]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[274]####
        taskinfo.setInstance(this);//####[274]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev) {//####[274]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[274]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[274]####
        // ensure Method variable is set//####[274]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[274]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[274]####
        }//####[274]####
        taskinfo.setQueueArgIndexes(0);//####[274]####
        taskinfo.setIsPipeline(true);//####[274]####
        taskinfo.setTaskIdArgIndexes(1, 2);//####[274]####
        taskinfo.addDependsOn(remainingNodes);//####[274]####
        taskinfo.addDependsOn(prev);//####[274]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[274]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[274]####
        taskinfo.setInstance(this);//####[274]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev) {//####[274]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[274]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[274]####
        // ensure Method variable is set//####[274]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[274]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[274]####
        }//####[274]####
        taskinfo.setQueueArgIndexes(1);//####[274]####
        taskinfo.setIsPipeline(true);//####[274]####
        taskinfo.setTaskIdArgIndexes(2);//####[274]####
        taskinfo.addDependsOn(prev);//####[274]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[274]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[274]####
        taskinfo.setInstance(this);//####[274]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev) {//####[274]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[274]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[274]####
        // ensure Method variable is set//####[274]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[274]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[274]####
        }//####[274]####
        taskinfo.setQueueArgIndexes(1);//####[274]####
        taskinfo.setIsPipeline(true);//####[274]####
        taskinfo.setTaskIdArgIndexes(0, 2);//####[274]####
        taskinfo.addDependsOn(processed);//####[274]####
        taskinfo.addDependsOn(prev);//####[274]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[274]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[274]####
        taskinfo.setInstance(this);//####[274]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev) {//####[274]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[274]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[274]####
        // ensure Method variable is set//####[274]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[274]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[274]####
        }//####[274]####
        taskinfo.setQueueArgIndexes(0, 1);//####[274]####
        taskinfo.setIsPipeline(true);//####[274]####
        taskinfo.setTaskIdArgIndexes(2);//####[274]####
        taskinfo.addDependsOn(prev);//####[274]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[274]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[274]####
        taskinfo.setInstance(this);//####[274]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, BlockingQueue<Schedule> prev) {//####[274]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[274]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[274]####
        // ensure Method variable is set//####[274]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[274]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[274]####
        }//####[274]####
        taskinfo.setQueueArgIndexes(2);//####[274]####
        taskinfo.setIsPipeline(true);//####[274]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[274]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[274]####
        taskinfo.setInstance(this);//####[274]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, BlockingQueue<Schedule> prev) {//####[274]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[274]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[274]####
        // ensure Method variable is set//####[274]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[274]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[274]####
        }//####[274]####
        taskinfo.setQueueArgIndexes(2);//####[274]####
        taskinfo.setIsPipeline(true);//####[274]####
        taskinfo.setTaskIdArgIndexes(0);//####[274]####
        taskinfo.addDependsOn(processed);//####[274]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[274]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[274]####
        taskinfo.setInstance(this);//####[274]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, BlockingQueue<Schedule> prev) {//####[274]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[274]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[274]####
        // ensure Method variable is set//####[274]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[274]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[274]####
        }//####[274]####
        taskinfo.setQueueArgIndexes(0, 2);//####[274]####
        taskinfo.setIsPipeline(true);//####[274]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[274]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[274]####
        taskinfo.setInstance(this);//####[274]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, TaskID<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev) {//####[274]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[274]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, TaskID<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[274]####
        // ensure Method variable is set//####[274]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[274]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[274]####
        }//####[274]####
        taskinfo.setQueueArgIndexes(2);//####[274]####
        taskinfo.setIsPipeline(true);//####[274]####
        taskinfo.setTaskIdArgIndexes(1);//####[274]####
        taskinfo.addDependsOn(remainingNodes);//####[274]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[274]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[274]####
        taskinfo.setInstance(this);//####[274]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev) {//####[274]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[274]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[274]####
        // ensure Method variable is set//####[274]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[274]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[274]####
        }//####[274]####
        taskinfo.setQueueArgIndexes(2);//####[274]####
        taskinfo.setIsPipeline(true);//####[274]####
        taskinfo.setTaskIdArgIndexes(0, 1);//####[274]####
        taskinfo.addDependsOn(processed);//####[274]####
        taskinfo.addDependsOn(remainingNodes);//####[274]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[274]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[274]####
        taskinfo.setInstance(this);//####[274]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev) {//####[274]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[274]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[274]####
        // ensure Method variable is set//####[274]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[274]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[274]####
        }//####[274]####
        taskinfo.setQueueArgIndexes(0, 2);//####[274]####
        taskinfo.setIsPipeline(true);//####[274]####
        taskinfo.setTaskIdArgIndexes(1);//####[274]####
        taskinfo.addDependsOn(remainingNodes);//####[274]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[274]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[274]####
        taskinfo.setInstance(this);//####[274]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev) {//####[274]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[274]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[274]####
        // ensure Method variable is set//####[274]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[274]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[274]####
        }//####[274]####
        taskinfo.setQueueArgIndexes(1, 2);//####[274]####
        taskinfo.setIsPipeline(true);//####[274]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[274]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[274]####
        taskinfo.setInstance(this);//####[274]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev) {//####[274]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[274]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[274]####
        // ensure Method variable is set//####[274]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[274]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[274]####
        }//####[274]####
        taskinfo.setQueueArgIndexes(1, 2);//####[274]####
        taskinfo.setIsPipeline(true);//####[274]####
        taskinfo.setTaskIdArgIndexes(0);//####[274]####
        taskinfo.addDependsOn(processed);//####[274]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[274]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[274]####
        taskinfo.setInstance(this);//####[274]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev) {//####[274]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[274]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[274]####
    }//####[274]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[274]####
        // ensure Method variable is set//####[274]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[274]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[274]####
        }//####[274]####
        taskinfo.setQueueArgIndexes(0, 1, 2);//####[274]####
        taskinfo.setIsPipeline(true);//####[274]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[274]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[274]####
        taskinfo.setInstance(this);//####[274]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[274]####
    }//####[274]####
    public void __pt__recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, Schedule prev) {//####[274]####
        recursiveScheduleGeneration(processed, remainingNodes, prev);//####[275]####
        _threads.release();//####[276]####
    }//####[277]####
//####[277]####
//####[291]####
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
	 *///####[291]####
    private void recursiveScheduleGeneration(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, Schedule prev) {//####[291]####
        _recursiveCalls.incrementAndGet();//####[292]####
        if (remainingNodes.size() == 0) //####[295]####
        {//####[295]####
            Schedule finalSchedule = prev;//####[296]####
            compareSchedules(finalSchedule);//####[299]####
        } else {//####[300]####
            for (int i = 0; i < remainingNodes.size(); i++) //####[301]####
            {//####[301]####
                Schedule newSchedule;//####[302]####
                for (int j = 1; j <= _numberOfCores; j++) //####[305]####
                {//####[305]####
                    List<AlgorithmNode> newProcessed = new ArrayList<AlgorithmNode>(processed);//####[309]####
                    AlgorithmNode node = remainingNodes.get(i).createClone();//####[310]####
                    node.setCore(j);//####[311]####
                    newProcessed.add(node);//####[312]####
                    Set<AlgorithmNode> algNodesSet = new HashSet<AlgorithmNode>(newProcessed);//####[314]####
                    if (checkValidSchedule(newProcessed)) //####[316]####
                    {//####[316]####
                        newSchedule = prev.getNextSchedule(node);//####[317]####
                        if ((newSchedule.getTotalTime() >= _bestTime)) //####[320]####
                        {//####[320]####
                            continue;//####[321]####
                        }//####[322]####
                    } else {//####[323]####
                        break;//####[324]####
                    }//####[325]####
                    if (_uniqueProcessed.contains(algNodesSet)) //####[336]####
                    {//####[336]####
                        continue;//####[337]####
                    } else {//####[339]####
                        _uniqueProcessed.add(algNodesSet);//####[340]####
                    }//####[341]####
                    List<AlgorithmNode> newRemaining = new ArrayList<AlgorithmNode>(remainingNodes);//####[344]####
                    newRemaining.remove(i);//####[345]####
                    if (_dag.getNodeByName(node.getNodeName()).getSuccessors().size() > 1 && _threads.tryAcquire()) //####[347]####
                    {//####[347]####
                        recursiveScheduleGenerationTask(newProcessed, newRemaining, newSchedule);//####[348]####
                    } else {//####[349]####
                        recursiveScheduleGeneration(newProcessed, newRemaining, newSchedule);//####[350]####
                    }//####[351]####
                    List<Integer> coresAssigned = new ArrayList<Integer>();//####[366]####
                    for (AlgorithmNode algNode : processed) //####[367]####
                    {//####[367]####
                        coresAssigned.add(algNode.getCore());//####[368]####
                    }//####[369]####
                    if (!coresAssigned.contains(node.getCore())) //####[371]####
                    {//####[371]####
                        break;//####[372]####
                    }//####[373]####
                }//####[374]####
            }//####[375]####
        }//####[376]####
    }//####[377]####
//####[379]####
    private void setNewBestSchedule(Schedule finalSchedule) {//####[379]####
        for (int i = 0; i < finalSchedule.getSizeOfSchedule(); i++) //####[380]####
        {//####[380]####
            NodeSchedule nodeSchedule = new NodeScheduleImp(finalSchedule.getNodeStartTime(i), finalSchedule.getNodeCore(i));//####[381]####
            _currentBestSchedule.put(finalSchedule.getNodeName(i), nodeSchedule);//####[382]####
        }//####[385]####
    }//####[386]####
//####[395]####
    /**
	 * This method determines whether a schedule is valid. It does this by ensuring a nodes predecessors are scheduled
	 * before the current node
	 *
	 * @param schedule
	 * @return true if the schedule is valid, false if not
	 *///####[395]####
    private boolean checkValidSchedule(List<AlgorithmNode> schedule) {//####[395]####
        if (schedule == null) //####[396]####
        {//####[396]####
            return false;//####[397]####
        }//####[398]####
        Node currentNode = _dag.getNodeByName(schedule.get(schedule.size() - 1).getNodeName());//####[401]####
        List<Node> predecessors = currentNode.getPredecessors();//####[402]####
        if (predecessors.size() == 0) //####[405]####
        {//####[405]####
            return true;//####[406]####
        } else if (schedule.size() == 1) //####[407]####
        {//####[407]####
            return false;//####[408]####
        }//####[409]####
        int counter = 0;//####[412]####
        for (int i = schedule.size() - 2; i >= 0; i--) //####[413]####
        {//####[413]####
            for (Node preNode : predecessors) //####[414]####
            {//####[414]####
                if (schedule.get(i).getNodeName().equals(preNode.getName())) //####[415]####
                {//####[415]####
                    counter++;//####[416]####
                    break;//####[417]####
                }//####[418]####
            }//####[419]####
        }//####[420]####
        if (counter != predecessors.size()) //####[423]####
        {//####[423]####
            return false;//####[424]####
        }//####[425]####
        return true;//####[426]####
    }//####[427]####
//####[435]####
    /**
	 * Calculates the time cost of executing the given schedule, returning a complete ScheduleImp object.
	 * @param algNodes - A {@code List<AlgorithmNode>} given in the order of execution
	 * @return - ScheduleImp object with cost and execution time information
	 *///####[435]####
    @Deprecated//####[435]####
    private ScheduleImp calculateTotalTime(List<AlgorithmNode> algNodes) {//####[435]####
        List<Node> nodes = new ArrayList<Node>();//####[437]####
        for (AlgorithmNode algNode : algNodes) //####[440]####
        {//####[440]####
            nodes.add(_dag.getNodeByName(algNode.getNodeName()));//####[441]####
        }//####[442]####
        List<AlgorithmNode> latestAlgNodeInSchedules = Arrays.asList(new AlgorithmNode[_numberOfCores]);//####[446]####
        ScheduleImp st = new ScheduleImp(algNodes, _numberOfCores);//####[449]####
        for (AlgorithmNode currentAlgNode : algNodes) //####[452]####
        {//####[452]####
            Node currentNode = nodes.get(algNodes.indexOf(currentAlgNode));//####[453]####
            int highestCost = 0;//####[454]####
            for (Node node : currentNode.getPredecessors()) //####[457]####
            {//####[457]####
                int cost = st.getNodeStartTime(getIndexOfList(node, algNodes)) + node.getWeight();//####[459]####
                if (!(algNodes.get(getIndexOfList(node, algNodes)).getCore() == currentAlgNode.getCore())) //####[460]####
                {//####[460]####
                    cost += currentNode.getInArc(node).getWeight();//####[462]####
                }//####[463]####
                if (cost > highestCost) //####[465]####
                {//####[465]####
                    highestCost = cost;//####[466]####
                }//####[467]####
            }//####[468]####
            AlgorithmNode latestNode = latestAlgNodeInSchedules.get(currentAlgNode.getCore() - 1);//####[471]####
            if (latestNode != null) //####[472]####
            {//####[472]####
                Node previousNode = _dag.getNodeByName(latestNode.getNodeName());//####[473]####
                int cost = previousNode.getWeight() + st.getNodeStartTime(algNodes.indexOf(latestNode));//####[474]####
                if (cost > highestCost) //####[475]####
                {//####[475]####
                    highestCost = cost;//####[476]####
                }//####[477]####
            }//####[478]####
            latestAlgNodeInSchedules.set(currentAlgNode.getCore() - 1, currentAlgNode);//####[481]####
            st.setStartTimeForNode(highestCost, algNodes.indexOf(currentAlgNode));//####[484]####
        }//####[485]####
        setTimeForSchedule(latestAlgNodeInSchedules, algNodes, st);//####[487]####
        return st;//####[489]####
    }//####[490]####
//####[500]####
    /**
	 * Calculates and sets the total time in the {@code ScheduleImp} object given.
	 * Main purpose is to make the code more readable.
	 * @param latestAlgNodeInSchedules - {@code List<AlgorithmNode>} containing the last node in each processor
	 * @param algNodes - the same {@code List<AlgorithmnNode>} used to construct the {@code ScheduleImp} object
	 * @param st - {@code ScheduleImp} object to set the total time of
	 *///####[500]####
    @Deprecated//####[500]####
    private void setTimeForSchedule(List<AlgorithmNode> latestAlgNodeInSchedules, List<AlgorithmNode> algNodes, ScheduleImp st) {//####[500]####
        int totalTime = 0;//####[501]####
        for (int i = 1; i <= _numberOfCores; i++) //####[502]####
        {//####[502]####
            AlgorithmNode latestAlgNode = latestAlgNodeInSchedules.get(i - 1);//####[503]####
            int timeTaken = 0;//####[505]####
            if (latestAlgNode != null) //####[506]####
            {//####[506]####
                timeTaken = st.getNodeStartTime(algNodes.indexOf(latestAlgNode)) + _dag.getNodeByName(latestAlgNode.getNodeName()).getWeight();//####[507]####
            }//####[508]####
            if (timeTaken > totalTime) //####[510]####
            {//####[510]####
                totalTime = timeTaken;//####[511]####
            }//####[512]####
        }//####[513]####
        st.setTotalTime(totalTime);//####[515]####
    }//####[516]####
//####[525]####
    /**
	 * Finds and returns the index position of the corresponding {@code AlgorithmNode} within the given {@code List<AlgorithmNode}
	 * @param node - {@code Node} to find the corresponding index position for
	 * @param algNodes - {@code List<AlgorithmNode>} to find the index for
	 * @return the index position of the corresponding {@code AlgorithmNode} object
	 *///####[525]####
    @Deprecated//####[525]####
    private int getIndexOfList(Node node, List<AlgorithmNode> algNodes) {//####[525]####
        for (AlgorithmNode algNode : algNodes) //####[526]####
        {//####[526]####
            if (node.getName().equals(algNode.getNodeName())) //####[527]####
            {//####[527]####
                return algNodes.indexOf(algNode);//####[528]####
            }//####[529]####
        }//####[530]####
        return -1;//####[531]####
    }//####[532]####
//####[535]####
    @Override//####[535]####
    public HashMap<String, NodeSchedule> getCurrentBestSchedule() {//####[535]####
        return _currentBestSchedule;//####[536]####
    }//####[537]####
//####[540]####
    @Override//####[540]####
    public int getBestTotalTime() {//####[540]####
        return _bestTime;//####[541]####
    }//####[542]####
//####[550]####
    /**
	 * The wrapper methods purely for testing. (as the methods were declared to be private)
	 * @param algNodes
	 * @return
	 *///####[550]####
    @Deprecated//####[550]####
    public ScheduleImp calculateTotalTimeWrapper(List<AlgorithmNode> algNodes) {//####[550]####
        return calculateTotalTime(algNodes);//####[551]####
    }//####[552]####
//####[554]####
    public boolean checkValidScheduleWrapper(List<AlgorithmNode> s1) {//####[554]####
        return checkValidSchedule(s1);//####[555]####
    }//####[556]####
}//####[556]####
