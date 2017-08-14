package visualisation;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;
import org.junit.Test;

import implementations.ConversionImp;
import implementations.algorithm.AlgorithmImp;
import implementations.io.InputImp;
import interfaces.Conversion;
import interfaces.io.Input;
import interfaces.structures.DAG;
import interfaces.structures.NodeSchedule;
import interfaces.structures.SchedulerTime;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTable;

/**
 * A class to test the JGraphT library for the MVC view 
 * @author dariusau
 *
 */
public class GraphStreamView extends JFrame implements GraphView {
	
	private static HashMap<interfaces.structures.Node,Node> _graphNodeNodeMap;
	private static HashMap<String,Node> _graphStringNodeMap;
	private static List<Color> _processorColours;
	private static DAG _dag;
	private static int _processorCount;
	private static Graph _GRAPH;
	
	public static void main(String[] args){
			Input input = new InputImp("testMultipleInputs.dot", "2");
			Conversion conversion = new ConversionImp(input);

			DAG dag = conversion.getDAG();
			GraphStreamView g = new GraphStreamView(dag,input.getProcessorCount());
			interfaces.structures.Node test = dag.getNodeByName("a");
			
			Timer timer = new Timer();
			timer.schedule(new TimerTask(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					AlgorithmImp alg = new AlgorithmImp(dag, input.getProcessorCount());
					HashMap<String,NodeSchedule> currentSchedule = alg.getCurrentBestSchedule();
					
					
					Iterator itSchedule = currentSchedule.entrySet().iterator();
					while (itSchedule.hasNext()) {
						Map.Entry pair = (Map.Entry)itSchedule.next();
						
											
						Node currentNode = _graphStringNodeMap.get((String) pair.getKey());
						
						currentNode.addAttribute("ui.color", _processorColours.get(((NodeSchedule) pair.getValue()).getBestProcessor()-1));
						currentNode.addAttribute("bestStartTime", ((NodeSchedule) pair.getValue()).getBestStartTime());
						/*
						if (((NodeSchedule) pair.getValue()).getBestProcessor() == 1){
							currentNode.addAttribute("ui.color", Color.BLUE);
						} else {
							currentNode.addAttribute("ui.color", Color.YELLOW);
						}
						*/
						
					    
					   
					    //it.remove(); // avoids a ConcurrentModificationException
					}
					
					
					
				}
				
			}, 2*1000);
			

		
	}
	
	
	
	public GraphStreamView(DAG dag, int processorCount){
		
		//Initialising fields
		_graphNodeNodeMap = new HashMap<interfaces.structures.Node,Node>();
		_graphStringNodeMap = new HashMap<String,Node>();
		_processorColours = new ArrayList<Color>();
		_processorCount = processorCount;
		
		//Gets random colours for different processors
		setProcessorColours();
		_dag = dag;
		
		List<interfaces.structures.Node> nodes = dag.getAllNodes();
		
		
		//Create a graph visual
		_GRAPH = new SingleGraph("Tutorial 1");
		
		//Stylesheet settings - needs to be modified
		_GRAPH.setAttribute("stylesheet", 
				"node { "
		        + "     shape: rounded-box; "
		        + "     padding: 5px;"
		        + "		size: 20px; "
		        + "     fill-mode: dyn-plain; "
		        + "     stroke-mode: plain; "
		        + "     size-mode: fit; "
		        + "} "
				+"node.marked { "
		        + "     shape: rounded-box; "
		        + "     padding: 5px; "
		        + "     fill-color: green; "
		        + "     stroke-mode: plain; "
		        + "     size-mode: fit; "
		        + "} "
		        + "edge { "
		        + "     shape: freeplane;"
		        + "		fill-color: black; "
		        + "}"
		        + "edge.marked { "
		        + "     fill-color: black;"
		        + "}");
		
		int count = 0;
		
		//Loop through all nodes
		for (interfaces.structures.Node currentNode : nodes){
			Node graphNode = _GRAPH.addNode(currentNode.getName());
			
			//Add to hashmap mapping our Node objects to GraphStream's Node objects
			_graphNodeNodeMap.put(currentNode, graphNode);
			
			//Add to hashmap mapping node names to GraphStream's Node objects
			_graphStringNodeMap.put(currentNode.getName(), graphNode);
			
			graphNode.setAttribute("ui.label", currentNode.getName());
			
			
			//Setting it to top of graph - needs editing with multiple start points
			if (_dag.getStartNodes().contains(currentNode)){
				count++;
				graphNode.addAttribute("layout.frozen");
				
				if (currentNode.getName().equals("a")){
				graphNode.setAttribute("xy", -1, 100);
				} else {
					graphNode.setAttribute("xy",1,100);
				}
				
			}
		}
		
		//Looping through all nodes
		Iterator it = _graphNodeNodeMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry)it.next();
			interfaces.structures.Node dagNode = (interfaces.structures.Node) pair.getKey();
			List<interfaces.structures.Node> successors = dagNode.getSuccessors();
		    
			//Drawing directed arcs from current node to all predecessors
		    for (interfaces.structures.Node succ : successors){
		    	String arcName = dagNode.getName() + succ.getName();
		    	_GRAPH.addEdge(arcName,dagNode.getName(),succ.getName(),true);
		    }
		    //it.remove(); // avoids a ConcurrentModificationException
		}
		
		
		/*
		Node A = graph.addNode("A" );
		Node B = graph.addNode("B" );
		Node C = graph.addNode("C" );
		
		A.setAttribute("ui.label", 3);
		B.setAttribute("ui.label", 2);
		C.setAttribute("ui.label", 4); 
		A.addAttribute("ui.style", "fill-color: rgb(0,100,255); size: 40px;");
		graph.addEdge("AB", "A", "B");
		graph.addEdge("BC", "B", "C");
		graph.addEdge("CA", "C", "A");
		*/
		
		Viewer viewer = _GRAPH.display();
	}
	@Override
	public void addButtonListener(ActionListener listener) {
		// TODO Auto-generated method stub
		
	}
	
	/*
	public void finalGrayArcs(){
		for (Edge edge : _GRAPH.getEachEdge()){
			Node nodeStart = edge.getNode0();
			Node nodeEnd = edge.getNode1();
			if (nodeStart.getAttribute("ui.color").equals(nodeEnd.getAttribute("ui.color")) 
					&& (int)nodeStart.getAttribute("bestStartTime") < (int)nodeEnd.getAttribute("bestStartTime")){
				System.out.println((String)nodeStart.getAttribute("ui.label") + ":" + nodeStart.getAttribute("bestStartTime"));
				System.out.println((String)nodeEnd.getAttribute("ui.label") + ":" + nodeEnd.getAttribute("bestStartTime") + "----");
				edge.setAttribute("ui.class", "marked");
			}
		}
	}
	*/
	/**
	 * Interesting method??? Review this method if needed
	 * @param node
	 */
	public void changeNodeColour(interfaces.structures.Node node){
		Node graphNode = _graphNodeNodeMap.get(node);
		graphNode.setAttribute("ui.class","marked");
	}
	
	/**
	 * Method to assign different random colours to every processor
	 * https://stackoverflow.com/questions/4246351/creating-random-colour-in-java
	 */
	private void setProcessorColours(){
		Random randomNum = new Random();
		for (int i=0; i<_processorCount; i++){
			float r = randomNum.nextFloat();
			float g = randomNum.nextFloat();
			float b = randomNum.nextFloat();
			
			Color newColour = new Color(r,g,b);
			_processorColours.add(newColour);
		}
	}
	
	
	
	

	
}
