package visualisation;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.graphicGraph.stylesheet.StyleConstants.Units;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

import implementations.io.Conversion;
import implementations.algorithm.AlgorithmImp;
import implementations.io.InputImp;
import implementations.structures.DAGImp;
import interfaces.io.Input;
import interfaces.structures.DAG;
import interfaces.structures.NodeSchedule;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
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
import javax.swing.JPanel;
import javax.swing.JTable;

/**
 * A class to test the JGraphT library for the MVC view 
 * @author dariusau
 *
 */
//public class GraphStreamView extends JFrame implements GraphView {
public class GraphStreamView implements GraphView {
	
	private static HashMap<interfaces.structures.Node,Node> _graphNodeNodeMap;
	private static HashMap<String,Node> _graphStringNodeMap;
	public static List<Color> _processorColours;
	private static DAG _dag;
	private static int _processorCount;
	private static Graph _GRAPH;
	
	
	/**
	 * GraphStreamView constructor
	 * @param dag
	 * @param processorCount
	 */
	public GraphStreamView(int processorCount){
		
		//Initialising fields
		_graphNodeNodeMap = new HashMap<interfaces.structures.Node,Node>();
		_graphStringNodeMap = new HashMap<String,Node>();
		_processorColours = new ArrayList<Color>();
		_processorCount = processorCount;
		
		//Gets random colours for different processors
		setProcessorColours();
		_dag = DAGImp.getInstance();
		
		List<interfaces.structures.Node> nodes = _dag.getAllNodes();
		
		
		//Create a graph visual
		_GRAPH = new SingleGraph("Tutorial 1");
		
		//Stylesheet settings - needs to be modified
		_GRAPH.setAttribute("stylesheet", 
				"node { "
		        + "     shape: circle; "
		        + "		size: 30px; "
		        + "     fill-mode: dyn-plain; "
		        + "     stroke-mode: plain; "
		        + "		stroke-color: black;"
		        + "		fill-color: white;"
		        + "		text-alignment: center;"
		        + "		text-size: 15px;"
		        + "		text-padding: 5px;"
		        + "} "
		        + "edge { "
		        + "		fill-color: black; "
		        + "}");
		
		//Loop through all nodes
		for (interfaces.structures.Node currentNode : nodes){
			Node graphNode = _GRAPH.addNode(currentNode.getName());
			
			//Add to hashmap mapping our Node objects to GraphStream's Node objects
			_graphNodeNodeMap.put(currentNode, graphNode);
			
			//Add to hashmap mapping node names to GraphStream's Node objects
			_graphStringNodeMap.put(currentNode.getName(), graphNode);
			
			graphNode.setAttribute("ui.label", currentNode.getName());
			
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
		}
		
		
	}
	@Override
	public void addButtonListener(ActionListener listener) {
		// TODO Auto-generated method stub
		
	}
	
	
	/**
	 * Interesting method??? Review this method if needed
	 * @param node
	 */
	/*
	public void changeNodeColour(interfaces.structures.Node node){
		Node graphNode = _graphNodeNodeMap.get(node);
		graphNode.setAttribute("ui.class","marked");
	}*/
	
	/**
	 * method to return GraphStream graph in a JPanel. Must call the constructor before calling this.
	 * @author Pulkit
	 * @return
	 */
	public JPanel getPanel(){
		JPanel jp = new JPanel();
		Viewer viewer = new Viewer(_GRAPH, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		
		//Creating a JPanel with BorderLayout properties in order to display the GraphStream view
		jp.setLayout(new BorderLayout());
		viewer.enableAutoLayout();
        ViewPanel viewPanel = viewer.addDefaultView(false);
        jp.add(viewPanel, BorderLayout.CENTER);
        return jp;
	}
	/**
	 * Method to assign different random colours to every processor
	 * https://stackoverflow.com/questions/4246351/creating-random-colour-in-java
	 */
	private void setProcessorColours(){
		Random randomNum = new Random();
		for (int i=0; i<_processorCount; i++){
			float r = (float) (randomNum.nextFloat()/2f + 0.5);
			float g = (float) (randomNum.nextFloat()/2f + 0.5);
			float b = (float) (randomNum.nextFloat()/2f + 0.5);
			
			Color newColour = new Color(r,g,b);
			_processorColours.add(newColour);
		}
	}
	
	public static void updateNodeColor(String nodeName, int coreNumber){
		Node graphNode = _graphStringNodeMap.get(nodeName);
		graphNode.addAttribute("ui.color", _processorColours.get(coreNumber-1));
	}



	@Override
	public JTable getTable() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Getter for name-to-gsnode hashmap
	 * @return
	 */
	public HashMap<String,Node> getStringNodeHashMap(){
		return _graphStringNodeMap;
	}
}