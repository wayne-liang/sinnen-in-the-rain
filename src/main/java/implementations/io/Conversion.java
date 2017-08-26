package implementations.io;

import implementations.structures.ArcImpl;
import implementations.structures.DAGImp;
import implementations.structures.NodeImp;
import interfaces.io.Input;
import interfaces.structures.Arc;
import interfaces.structures.DAG;
import interfaces.structures.Node;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class converts the raw input data into a DAG object.
 * @see Input for specifications on how data is passed in.
 *
 * @author Daniel
 */
public class Conversion {
	/**
	 * The DAG data read from the file and then processed by Input class
	 */
	private List<String[]> _graphData;

    /**
     * Constructor for Conversion module.
     * @param input - Input class
     */
    public Conversion(Input input) {
        _graphData = input.getGraphData();
        generateDAG();
    }

    /**
     * Generate a DAG from raw data
	 * This is done by splitting the values in the string array on the spaces.
	 * If the length of the value is greater than 2, it indicates it is an arc, otherwise it is a node.
	 * Save the data accordingly as an arc or node and then add it to the DAG.
	 *
	 * Also identify and add the root nodes to the DAG.
	 */
    private void generateDAG() {
        HashMap<String, Node> nodes = new HashMap<>();

        for (String[] values : _graphData) {
            String name = values[0];
            int weight = Integer.valueOf(values[1]);

			//Split string on spaces
			String[] namesArray = name.split("\\s+");
            if (namesArray.length == 2) { //If it's an arc
                Node srcNode = nodes.get(namesArray[0]);
                Node destNode = nodes.get(namesArray[1]);

                Arc arc = new ArcImpl(weight, srcNode, destNode);

                srcNode.addOutArc(arc);
                destNode.addInArc(arc);
            } else { //Else it's a node
                Node node = new NodeImp(name, weight);
                nodes.put(name, node);
            }
        }

        DAG dag = DAGImp.getInstance();

		//Add the root nodes to the DAG
		dag.addStartNodes(getRootNodes(nodes));

		//Add to the DAG object all the nodes
		nodes.values().forEach(dag::add);
    }

	/**
	 * This method returns the root node in a graph. This is done by finding all
	 * nodes which do not have any incoming arcs.
	 *
	 * @param nodes - Hashmap of <Node name, Node object>
	 * @return List of nodes which are the root node in the graph
	 */
	private List<Node> getRootNodes(HashMap<String, Node> nodes) {
    	return nodes.values().stream()
				.filter(n -> n.getPredecessors().size() == 0)
				.collect(Collectors.toList());
	}
}
