package visualisation;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jgraph.JGraph;
import org.jgrapht.DirectedGraph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphModelAdapter;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedGraph;

class JGraphTView extends JFrame{
	
	private JGraphModelAdapter m_jgAdapter;
	public static void main(String[] args){
		JGraphTView jg = new JGraphTView();
		JFrame frame = new JFrame();
		frame.getContentPane().add(jg);
		frame.setVisible(true);
	}
	public JGraphTView(){
		DirectedGraph<String, DefaultEdge> g =
	            new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
	 
	// create a visualization using JGraph, via an adapter
	m_jgAdapter = new JGraphModelAdapter( g );
	 
	JGraph jgraph = new JGraph( m_jgAdapter );

	getContentPane(  ).add( jgraph );
	 
	// add some sample data (graph manipulated via JGraphT)
	g.addVertex( "v1" );
	g.addVertex( "v2" );
	g.addVertex( "v3" );
	g.addVertex( "v4" );
	 
	g.addEdge( "v1", "v2" );
	g.addEdge( "v2", "v3" );
	g.addEdge( "v3", "v1" );
	g.addEdge( "v4", "v3" );
	}
}