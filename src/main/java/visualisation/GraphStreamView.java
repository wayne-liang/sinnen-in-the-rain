package visualisation;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTable;

/**
 * A class to test the JGraphT library for the MVC view 
 * @author dariusau
 *
 */
public class GraphStreamView extends JFrame implements GraphView {
	public static void main(String[] args){
		GraphStreamView graph = new GraphStreamView();
		
		
	}
	public GraphStreamView(){
		Graph graph = new SingleGraph("Tutorial 1");
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
		
		Viewer viewer = graph.display();
	}
	@Override
	public void addButtonListener(ActionListener listener) {
		// TODO Auto-generated method stub
		
	}

	
}
