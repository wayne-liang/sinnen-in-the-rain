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
            _model = TableModel.getInstance();//####[68]####
            _model.initModel(_currentBestSchedule, _dag, _numberOfCores);//####[69]####
            _chartModel = new BarChartModel();//####[71]####
            _schedule = new ComboView(_model, _dag, _numberOfCores, _chartModel);//####[73]####
        }//####[74]####
        _uniqueProcessed = Collections.synchronizedSet(new HashSet<Set<AlgorithmNode>>());//####[77]####
        produceSequentialSchedule();//####[79]####
        produceGreedySchedule();//####[80]####
        Schedule emptySchedule = new ScheduleImp(_numberOfCores);//####[82]####
        recursiveScheduleGeneration(new ArrayList<AlgorithmNode>(), AlgorithmNode.convertNodetoAlgorithmNode(_dag.getAllNodes()), emptySchedule);//####[83]####
        try {//####[85]####
            _threads.acquire(_numberOfThreads);//####[86]####
        } catch (InterruptedException ex) {//####[87]####
            ex.printStackTrace();//####[88]####
        }//####[89]####
        if (visualisation) //####[92]####
        {//####[92]####
            _model.changeData(_currentBestSchedule, _bestTime);//####[93]####
            _model = TableModel.resetInstance();//####[95]####
            Clock.getInstance().stopClock();//####[97]####
            _schedule.setStatusLabel(Clock.getInstance().getProcessStatus());//####[98]####
        }//####[99]####
    }//####[100]####
//####[105]####
    /**
	 * helper method for firing update.
	 *///####[105]####
    private void fireUpdateToGUI(int bestTime) {//####[105]####
        _chartModel.addDataToSeries(bestTime);//####[107]####
        _model.changeData(_currentBestSchedule, bestTime);//####[108]####
        _schedule.setBestTimeText(bestTime);//####[109]####
    }//####[110]####
//####[118]####
    /**
	 * This method will produce a sequential schedule to set the lower bound.
	 * 
	 * This will be used together will the greedy schedule to bound
	 * the DFS.
	 *///####[118]####
    private void produceSequentialSchedule() {//####[118]####
        List<Node> reachableNodes = new ArrayList<Node>();//####[119]####
        List<Node> completedNodes = new ArrayList<Node>();//####[120]####
        List<Node> remainingNodes = new ArrayList<Node>();//####[121]####
        reachableNodes.addAll(_dag.getStartNodes());//####[123]####
        remainingNodes.addAll(_dag.getAllNodes());//####[124]####
        Schedule schedule = new ScheduleImp(_numberOfCores);//####[126]####
        while (!reachableNodes.isEmpty()) //####[128]####
        {//####[128]####
            Node toBeScheduled = reachableNodes.get(0);//####[129]####
            AlgorithmNode algNode = new AlgorithmNodeImp(toBeScheduled.getName());//####[131]####
            algNode.setCore(1);//####[132]####
            schedule = schedule.getNextSchedule(algNode);//####[133]####
            completedNodes.add(toBeScheduled);//####[136]####
            reachableNodes.remove(toBeScheduled);//####[137]####
            remainingNodes.remove(toBeScheduled);//####[138]####
            for (Node rn : remainingNodes) //####[139]####
            {//####[139]####
                if (completedNodes.containsAll(rn.getPredecessors()) && !reachableNodes.contains(rn)) //####[140]####
                {//####[140]####
                    reachableNodes.add(rn);//####[141]####
                }//####[142]####
            }//####[143]####
        }//####[144]####
        setNewBestSchedule(schedule);//####[147]####
        _bestTime = schedule.getTotalTime();//####[148]####
    }//####[149]####
//####[157]####
    /**
	 * This method will produce a greedy schedule to set the lower bound.
	 * 
	 * This will be used together will the sequential schedule to bound
	 * the DFS.
	 *///####[157]####
    private void produceGreedySchedule() {//####[157]####
        List<Node> reachableNodes = new ArrayList<Node>();//####[158]####
        List<Node> completedNodes = new ArrayList<Node>();//####[159]####
        List<Node> remainingNodes = new ArrayList<Node>();//####[160]####
        reachableNodes.addAll(_dag.getStartNodes());//####[162]####
        remainingNodes.addAll(_dag.getAllNodes());//####[163]####
        Schedule schedule = new ScheduleImp(_numberOfCores);//####[165]####
        while (!reachableNodes.isEmpty()) //####[167]####
        {//####[167]####
            List<Integer> reachableAmount = new ArrayList<Integer>();//####[169]####
            for (Node n : reachableNodes) //####[170]####
            {//####[170]####
                reachableAmount.add(n.getSuccessors().size());//####[171]####
            }//####[172]####
            int maxIndex = reachableAmount.indexOf(Collections.max(reachableAmount));//####[173]####
            Node toBeScheduled = reachableNodes.get(maxIndex);//####[174]####
            List<Integer> earliestStartTimes = new ArrayList<Integer>();//####[177]####
            for (int i = 1; i <= _numberOfCores; i++) //####[178]####
            {//####[178]####
                int coreStart = schedule.getFinishTimeForCore(i);//####[179]####
                AlgorithmNode algNode = new AlgorithmNodeImp(toBeScheduled.getName());//####[180]####
                algNode.setCore(i);//####[181]####
                int depStart = schedule.getDependencyBasedStartTime(toBeScheduled, algNode);//####[182]####
                earliestStartTimes.add((coreStart > depStart) ? coreStart : depStart);//####[183]####
            }//####[184]####
            int earliestCoreNo = earliestStartTimes.indexOf(Collections.min(earliestStartTimes)) + 1;//####[185]####
            AlgorithmNode algNode = new AlgorithmNodeImp(toBeScheduled.getName());//####[187]####
            algNode.setCore(earliestCoreNo);//####[188]####
            schedule = schedule.getNextSchedule(algNode);//####[189]####
            completedNodes.add(toBeScheduled);//####[192]####
            reachableNodes.remove(toBeScheduled);//####[193]####
            remainingNodes.remove(toBeScheduled);//####[194]####
            for (Node rn : remainingNodes) //####[195]####
            {//####[195]####
                if (completedNodes.containsAll(rn.getPredecessors()) && !reachableNodes.contains(rn)) //####[196]####
                {//####[196]####
                    reachableNodes.add(rn);//####[197]####
                }//####[198]####
            }//####[199]####
        }//####[200]####
        if (schedule.getTotalTime() < _bestTime) //####[202]####
        {//####[202]####
            setNewBestSchedule(schedule);//####[203]####
            _bestTime = schedule.getTotalTime();//####[204]####
        }//####[205]####
    }//####[206]####
//####[213]####
    /**
	 * Purely for benchmarking purposes
	 *
	 * @return number of times the recursive method was called
	 *///####[213]####
    public int getRecursiveCalls() {//####[213]####
        return _recursiveCalls;//####[214]####
    }//####[215]####
//####[223]####
    /**
	 * This method is a thread-safe way of comparing a schedule against the current 
	 * best schedule so far, replacing it if the new one is better.
	 * 
	 * @param newSchedule 	- The new Schedule to compare 
	 *///####[223]####
    private synchronized void compareSchedules(Schedule newSchedule) {//####[223]####
        if (newSchedule.getTotalTime() < _bestTime) //####[224]####
        {//####[224]####
            setNewBestSchedule(newSchedule);//####[225]####
            _bestTime = newSchedule.getTotalTime();//####[226]####
        }//####[227]####
    }//####[228]####
//####[239]####
    private static volatile Method __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method = null;//####[239]####
    private synchronized static void __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet() {//####[239]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[239]####
            try {//####[239]####
                __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method = ParaTaskHelper.getDeclaredMethod(new ParaTaskHelper.ClassGetter().getCurrentClass(), "__pt__recursiveScheduleGenerationTask", new Class[] {//####[239]####
                    List.class, List.class, Schedule.class//####[239]####
                });//####[239]####
            } catch (Exception e) {//####[239]####
                e.printStackTrace();//####[239]####
            }//####[239]####
        }//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, Schedule prev) {//####[239]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[239]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[239]####
        // ensure Method variable is set//####[239]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[239]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[239]####
        }//####[239]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[239]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[239]####
        taskinfo.setInstance(this);//####[239]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, Schedule prev) {//####[239]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[239]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[239]####
        // ensure Method variable is set//####[239]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[239]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[239]####
        }//####[239]####
        taskinfo.setTaskIdArgIndexes(0);//####[239]####
        taskinfo.addDependsOn(processed);//####[239]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[239]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[239]####
        taskinfo.setInstance(this);//####[239]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, Schedule prev) {//####[239]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[239]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[239]####
        // ensure Method variable is set//####[239]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[239]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[239]####
        }//####[239]####
        taskinfo.setQueueArgIndexes(0);//####[239]####
        taskinfo.setIsPipeline(true);//####[239]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[239]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[239]####
        taskinfo.setInstance(this);//####[239]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, TaskID<List<AlgorithmNode>> remainingNodes, Schedule prev) {//####[239]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[239]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, TaskID<List<AlgorithmNode>> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[239]####
        // ensure Method variable is set//####[239]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[239]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[239]####
        }//####[239]####
        taskinfo.setTaskIdArgIndexes(1);//####[239]####
        taskinfo.addDependsOn(remainingNodes);//####[239]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[239]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[239]####
        taskinfo.setInstance(this);//####[239]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, Schedule prev) {//####[239]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[239]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[239]####
        // ensure Method variable is set//####[239]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[239]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[239]####
        }//####[239]####
        taskinfo.setTaskIdArgIndexes(0, 1);//####[239]####
        taskinfo.addDependsOn(processed);//####[239]####
        taskinfo.addDependsOn(remainingNodes);//####[239]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[239]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[239]####
        taskinfo.setInstance(this);//####[239]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, Schedule prev) {//####[239]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[239]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[239]####
        // ensure Method variable is set//####[239]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[239]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[239]####
        }//####[239]####
        taskinfo.setQueueArgIndexes(0);//####[239]####
        taskinfo.setIsPipeline(true);//####[239]####
        taskinfo.setTaskIdArgIndexes(1);//####[239]####
        taskinfo.addDependsOn(remainingNodes);//####[239]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[239]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[239]####
        taskinfo.setInstance(this);//####[239]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, Schedule prev) {//####[239]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[239]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[239]####
        // ensure Method variable is set//####[239]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[239]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[239]####
        }//####[239]####
        taskinfo.setQueueArgIndexes(1);//####[239]####
        taskinfo.setIsPipeline(true);//####[239]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[239]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[239]####
        taskinfo.setInstance(this);//####[239]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, Schedule prev) {//####[239]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[239]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[239]####
        // ensure Method variable is set//####[239]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[239]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[239]####
        }//####[239]####
        taskinfo.setQueueArgIndexes(1);//####[239]####
        taskinfo.setIsPipeline(true);//####[239]####
        taskinfo.setTaskIdArgIndexes(0);//####[239]####
        taskinfo.addDependsOn(processed);//####[239]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[239]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[239]####
        taskinfo.setInstance(this);//####[239]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, Schedule prev) {//####[239]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[239]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, Schedule prev, TaskInfo taskinfo) {//####[239]####
        // ensure Method variable is set//####[239]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[239]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[239]####
        }//####[239]####
        taskinfo.setQueueArgIndexes(0, 1);//####[239]####
        taskinfo.setIsPipeline(true);//####[239]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[239]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[239]####
        taskinfo.setInstance(this);//####[239]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, TaskID<Schedule> prev) {//####[239]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[239]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[239]####
        // ensure Method variable is set//####[239]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[239]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[239]####
        }//####[239]####
        taskinfo.setTaskIdArgIndexes(2);//####[239]####
        taskinfo.addDependsOn(prev);//####[239]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[239]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[239]####
        taskinfo.setInstance(this);//####[239]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, TaskID<Schedule> prev) {//####[239]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[239]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[239]####
        // ensure Method variable is set//####[239]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[239]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[239]####
        }//####[239]####
        taskinfo.setTaskIdArgIndexes(0, 2);//####[239]####
        taskinfo.addDependsOn(processed);//####[239]####
        taskinfo.addDependsOn(prev);//####[239]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[239]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[239]####
        taskinfo.setInstance(this);//####[239]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, TaskID<Schedule> prev) {//####[239]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[239]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[239]####
        // ensure Method variable is set//####[239]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[239]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[239]####
        }//####[239]####
        taskinfo.setQueueArgIndexes(0);//####[239]####
        taskinfo.setIsPipeline(true);//####[239]####
        taskinfo.setTaskIdArgIndexes(2);//####[239]####
        taskinfo.addDependsOn(prev);//####[239]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[239]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[239]####
        taskinfo.setInstance(this);//####[239]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, TaskID<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev) {//####[239]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[239]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, TaskID<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[239]####
        // ensure Method variable is set//####[239]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[239]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[239]####
        }//####[239]####
        taskinfo.setTaskIdArgIndexes(1, 2);//####[239]####
        taskinfo.addDependsOn(remainingNodes);//####[239]####
        taskinfo.addDependsOn(prev);//####[239]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[239]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[239]####
        taskinfo.setInstance(this);//####[239]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev) {//####[239]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[239]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[239]####
        // ensure Method variable is set//####[239]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[239]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[239]####
        }//####[239]####
        taskinfo.setTaskIdArgIndexes(0, 1, 2);//####[239]####
        taskinfo.addDependsOn(processed);//####[239]####
        taskinfo.addDependsOn(remainingNodes);//####[239]####
        taskinfo.addDependsOn(prev);//####[239]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[239]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[239]####
        taskinfo.setInstance(this);//####[239]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev) {//####[239]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[239]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[239]####
        // ensure Method variable is set//####[239]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[239]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[239]####
        }//####[239]####
        taskinfo.setQueueArgIndexes(0);//####[239]####
        taskinfo.setIsPipeline(true);//####[239]####
        taskinfo.setTaskIdArgIndexes(1, 2);//####[239]####
        taskinfo.addDependsOn(remainingNodes);//####[239]####
        taskinfo.addDependsOn(prev);//####[239]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[239]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[239]####
        taskinfo.setInstance(this);//####[239]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev) {//####[239]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[239]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[239]####
        // ensure Method variable is set//####[239]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[239]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[239]####
        }//####[239]####
        taskinfo.setQueueArgIndexes(1);//####[239]####
        taskinfo.setIsPipeline(true);//####[239]####
        taskinfo.setTaskIdArgIndexes(2);//####[239]####
        taskinfo.addDependsOn(prev);//####[239]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[239]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[239]####
        taskinfo.setInstance(this);//####[239]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev) {//####[239]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[239]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[239]####
        // ensure Method variable is set//####[239]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[239]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[239]####
        }//####[239]####
        taskinfo.setQueueArgIndexes(1);//####[239]####
        taskinfo.setIsPipeline(true);//####[239]####
        taskinfo.setTaskIdArgIndexes(0, 2);//####[239]####
        taskinfo.addDependsOn(processed);//####[239]####
        taskinfo.addDependsOn(prev);//####[239]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[239]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[239]####
        taskinfo.setInstance(this);//####[239]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev) {//####[239]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[239]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, TaskID<Schedule> prev, TaskInfo taskinfo) {//####[239]####
        // ensure Method variable is set//####[239]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[239]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[239]####
        }//####[239]####
        taskinfo.setQueueArgIndexes(0, 1);//####[239]####
        taskinfo.setIsPipeline(true);//####[239]####
        taskinfo.setTaskIdArgIndexes(2);//####[239]####
        taskinfo.addDependsOn(prev);//####[239]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[239]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[239]####
        taskinfo.setInstance(this);//####[239]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, BlockingQueue<Schedule> prev) {//####[239]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[239]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[239]####
        // ensure Method variable is set//####[239]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[239]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[239]####
        }//####[239]####
        taskinfo.setQueueArgIndexes(2);//####[239]####
        taskinfo.setIsPipeline(true);//####[239]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[239]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[239]####
        taskinfo.setInstance(this);//####[239]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, BlockingQueue<Schedule> prev) {//####[239]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[239]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[239]####
        // ensure Method variable is set//####[239]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[239]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[239]####
        }//####[239]####
        taskinfo.setQueueArgIndexes(2);//####[239]####
        taskinfo.setIsPipeline(true);//####[239]####
        taskinfo.setTaskIdArgIndexes(0);//####[239]####
        taskinfo.addDependsOn(processed);//####[239]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[239]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[239]####
        taskinfo.setInstance(this);//####[239]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, BlockingQueue<Schedule> prev) {//####[239]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[239]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, List<AlgorithmNode> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[239]####
        // ensure Method variable is set//####[239]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[239]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[239]####
        }//####[239]####
        taskinfo.setQueueArgIndexes(0, 2);//####[239]####
        taskinfo.setIsPipeline(true);//####[239]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[239]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[239]####
        taskinfo.setInstance(this);//####[239]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, TaskID<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev) {//####[239]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[239]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, TaskID<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[239]####
        // ensure Method variable is set//####[239]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[239]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[239]####
        }//####[239]####
        taskinfo.setQueueArgIndexes(2);//####[239]####
        taskinfo.setIsPipeline(true);//####[239]####
        taskinfo.setTaskIdArgIndexes(1);//####[239]####
        taskinfo.addDependsOn(remainingNodes);//####[239]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[239]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[239]####
        taskinfo.setInstance(this);//####[239]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev) {//####[239]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[239]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[239]####
        // ensure Method variable is set//####[239]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[239]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[239]####
        }//####[239]####
        taskinfo.setQueueArgIndexes(2);//####[239]####
        taskinfo.setIsPipeline(true);//####[239]####
        taskinfo.setTaskIdArgIndexes(0, 1);//####[239]####
        taskinfo.addDependsOn(processed);//####[239]####
        taskinfo.addDependsOn(remainingNodes);//####[239]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[239]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[239]####
        taskinfo.setInstance(this);//####[239]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev) {//####[239]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[239]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, TaskID<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[239]####
        // ensure Method variable is set//####[239]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[239]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[239]####
        }//####[239]####
        taskinfo.setQueueArgIndexes(0, 2);//####[239]####
        taskinfo.setIsPipeline(true);//####[239]####
        taskinfo.setTaskIdArgIndexes(1);//####[239]####
        taskinfo.addDependsOn(remainingNodes);//####[239]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[239]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[239]####
        taskinfo.setInstance(this);//####[239]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev) {//####[239]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[239]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(List<AlgorithmNode> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[239]####
        // ensure Method variable is set//####[239]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[239]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[239]####
        }//####[239]####
        taskinfo.setQueueArgIndexes(1, 2);//####[239]####
        taskinfo.setIsPipeline(true);//####[239]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[239]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[239]####
        taskinfo.setInstance(this);//####[239]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev) {//####[239]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[239]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(TaskID<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[239]####
        // ensure Method variable is set//####[239]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[239]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[239]####
        }//####[239]####
        taskinfo.setQueueArgIndexes(1, 2);//####[239]####
        taskinfo.setIsPipeline(true);//####[239]####
        taskinfo.setTaskIdArgIndexes(0);//####[239]####
        taskinfo.addDependsOn(processed);//####[239]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[239]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[239]####
        taskinfo.setInstance(this);//####[239]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev) {//####[239]####
        //-- execute asynchronously by enqueuing onto the taskpool//####[239]####
        return recursiveScheduleGenerationTask(processed, remainingNodes, prev, new TaskInfo());//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    private TaskID<Void> recursiveScheduleGenerationTask(BlockingQueue<List<AlgorithmNode>> processed, BlockingQueue<List<AlgorithmNode>> remainingNodes, BlockingQueue<Schedule> prev, TaskInfo taskinfo) {//####[239]####
        // ensure Method variable is set//####[239]####
        if (__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method == null) {//####[239]####
            __pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_ensureMethodVarSet();//####[239]####
        }//####[239]####
        taskinfo.setQueueArgIndexes(0, 1, 2);//####[239]####
        taskinfo.setIsPipeline(true);//####[239]####
        taskinfo.setParameters(processed, remainingNodes, prev);//####[239]####
        taskinfo.setMethod(__pt__recursiveScheduleGenerationTask_ListAlgorithmNode_ListAlgorithmNode_Schedule_method);//####[239]####
        taskinfo.setInstance(this);//####[239]####
        return TaskpoolFactory.getTaskpool().enqueue(taskinfo);//####[239]####
    }//####[239]####
    /**
	 * This method is a parallel wrapper method, essentially carrying out the same
	 * function as the original recursiveScheduleGeneration method, but on a new
	 * thread.
	 
	 * @param processed      - A list of processed nodes
	 * @param remainingNodes - A list of nodes remaining to be processed
	 * @param prev			 - The previous schedule. 
	 *///####[239]####
    public void __pt__recursiveScheduleGenerationTask(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, Schedule prev) {//####[239]####
        recursiveScheduleGeneration(processed, remainingNodes, prev);//####[240]####
        _threads.release();//####[241]####
    }//####[242]####
//####[242]####
//####[256]####
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
	 *///####[256]####
    private void recursiveScheduleGeneration(List<AlgorithmNode> processed, List<AlgorithmNode> remainingNodes, Schedule prev) {//####[256]####
        if (_visualisation) //####[257]####
        {//####[257]####
            _schedule.setCallsButtonText(_recursiveCalls++);//####[258]####
        }//####[259]####
        if (remainingNodes.size() == 0) //####[262]####
        {//####[262]####
            Schedule finalSchedule = prev;//####[263]####
            compareSchedules(finalSchedule);//####[266]####
        } else {//####[267]####
            for (int i = 0; i < remainingNodes.size(); i++) //####[268]####
            {//####[268]####
                Schedule newSchedule;//####[269]####
                for (int j = 1; j <= _numberOfCores; j++) //####[272]####
                {//####[272]####
                    List<AlgorithmNode> newProcessed = new ArrayList<AlgorithmNode>(processed);//####[276]####
                    AlgorithmNode node = remainingNodes.get(i).createClone();//####[277]####
                    node.setCore(j);//####[278]####
                    newProcessed.add(node);//####[279]####
                    Set<AlgorithmNode> algNodesSet = new HashSet<AlgorithmNode>(newProcessed);//####[281]####
                    if (checkValidSchedule(newProcessed)) //####[283]####
                    {//####[283]####
                        int idleTime = prev.getTotalIdleTime();//####[288]####
                        double maxIdleTime = 0;//####[290]####
                        for (int k = 1; k <= _numberOfCores; k++) //####[291]####
                        {//####[291]####
                            int processorIdleTime = prev.getTotalTime() - prev.getFinishTimeForCore(i);//####[292]####
                            if (maxIdleTime < processorIdleTime) //####[293]####
                            {//####[293]####
                                maxIdleTime = processorIdleTime;//####[294]####
                            }//####[295]####
                        }//####[296]####
                        double maxNodeWeight = 0;//####[298]####
                        double remainingTime = 0;//####[299]####
                        for (AlgorithmNode algNode : remainingNodes) //####[300]####
                        {//####[300]####
                            Node coNode = _dag.getNodeByName(algNode.getNodeName());//####[301]####
                            if (maxNodeWeight < coNode.getWeight()) //####[302]####
                            {//####[302]####
                                maxNodeWeight = coNode.getWeight();//####[303]####
                            }//####[304]####
                            remainingTime += coNode.getWeight();//####[305]####
                        }//####[306]####
                        double shortestTimePossible = Math.max(Math.ceil((remainingTime - idleTime + 0.0) / _numberOfCores), maxNodeWeight - maxIdleTime);//####[308]####
                        if (prev.getTotalTime() + shortestTimePossible >= _bestTime) //####[309]####
                        {//####[309]####
                            continue;//####[310]####
                        }//####[311]####
                        newSchedule = prev.getNextSchedule(node);//####[313]####
                        if ((newSchedule.getTotalTime() >= _bestTime)) //####[316]####
                        {//####[316]####
                            continue;//####[317]####
                        }//####[318]####
                    } else {//####[319]####
                        break;//####[320]####
                    }//####[321]####
                    if (_uniqueProcessed.contains(algNodesSet)) //####[332]####
                    {//####[332]####
                        continue;//####[333]####
                    } else {//####[335]####
                        _uniqueProcessed.add(algNodesSet);//####[336]####
                    }//####[337]####
                    List<AlgorithmNode> newRemaining = new ArrayList<AlgorithmNode>(remainingNodes);//####[341]####
                    newRemaining.remove(i);//####[342]####
                    List<Integer> coresAssigned = new ArrayList<Integer>();//####[357]####
                    for (AlgorithmNode algNode : processed) //####[358]####
                    {//####[358]####
                        coresAssigned.add(algNode.getCore());//####[359]####
                    }//####[360]####
                    if (!coresAssigned.contains(node.getCore())) //####[362]####
                    {//####[362]####
                        recursiveScheduleGeneration(newProcessed, newRemaining, newSchedule);//####[364]####
                        break;//####[365]####
                    } else {//####[366]####
                        if (_dag.getNodeByName(node.getNodeName()).getSuccessors().size() > 1 && _threads.tryAcquire()) //####[371]####
                        {//####[371]####
                            recursiveScheduleGenerationTask(newProcessed, newRemaining, newSchedule);//####[372]####
                        } else {//####[373]####
                            recursiveScheduleGeneration(newProcessed, newRemaining, newSchedule);//####[374]####
                        }//####[375]####
                    }//####[376]####
                }//####[377]####
            }//####[378]####
        }//####[379]####
    }//####[380]####
//####[382]####
    private void setNewBestSchedule(Schedule finalSchedule) {//####[382]####
        for (int i = 0; i < finalSchedule.getSizeOfSchedule(); i++) //####[383]####
        {//####[383]####
            NodeSchedule nodeSchedule = new NodeScheduleImp(finalSchedule.getNodeStartTime(i), finalSchedule.getNodeCore(i));//####[384]####
            _currentBestSchedule.put(finalSchedule.getNodeName(i), nodeSchedule);//####[385]####
        }//####[386]####
        if (_visualisation) //####[388]####
        {//####[388]####
            fireUpdateToGUI(finalSchedule.getTotalTime());//####[389]####
        }//####[390]####
    }//####[391]####
//####[400]####
    /**
	 * This method determines whether a schedule is valid. It does this by ensuring a nodes predecessors are scheduled
	 * before the current node
	 *
	 * @param schedule
	 * @return true if the schedule is valid, false if not
	 *///####[400]####
    private boolean checkValidSchedule(List<AlgorithmNode> schedule) {//####[400]####
        if (schedule == null) //####[401]####
        {//####[401]####
            return false;//####[402]####
        }//####[403]####
        Node currentNode = _dag.getNodeByName(schedule.get(schedule.size() - 1).getNodeName());//####[406]####
        List<Node> predecessors = currentNode.getPredecessors();//####[407]####
        if (predecessors.size() == 0) //####[410]####
        {//####[410]####
            return true;//####[411]####
        } else if (schedule.size() == 1) //####[412]####
        {//####[412]####
            return false;//####[413]####
        }//####[414]####
        int counter = 0;//####[417]####
        for (int i = schedule.size() - 2; i >= 0; i--) //####[418]####
        {//####[418]####
            for (Node preNode : predecessors) //####[419]####
            {//####[419]####
                if (schedule.get(i).getNodeName().equals(preNode.getName())) //####[420]####
                {//####[420]####
                    counter++;//####[421]####
                    break;//####[422]####
                }//####[423]####
            }//####[424]####
        }//####[425]####
        if (counter != predecessors.size()) //####[428]####
        {//####[428]####
            return false;//####[429]####
        }//####[430]####
        return true;//####[431]####
    }//####[432]####
//####[435]####
    @Override//####[435]####
    public HashMap<String, NodeSchedule> getCurrentBestSchedule() {//####[435]####
        return _currentBestSchedule;//####[436]####
    }//####[437]####
//####[440]####
    @Override//####[440]####
    public int getBestTotalTime() {//####[440]####
        return _bestTime;//####[441]####
    }//####[442]####
//####[447]####
    /**
	 * The wrapper method purely for testing. (as the method was declared to be private)
	 *///####[447]####
    public boolean checkValidScheduleWrapper(List<AlgorithmNode> s1) {//####[447]####
        return checkValidSchedule(s1);//####[448]####
    }//####[449]####
}//####[449]####
