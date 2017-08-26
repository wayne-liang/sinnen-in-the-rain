package interfaces.algorithm;

import implementations.algorithm.AlgorithmNodeImp;
import interfaces.structures.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * See the implementation class for more details.
 * @see implementations.algorithm.AlgorithmNodeImp
 * 
 * @author Daniel
 *
 */
public interface AlgorithmNode {
    /**
     * This static method converts a list of nodes to a list of algorithmNode objects
     *
     * @param nodes
     * @return a list of algorithmNode objects.
     */
    static List<AlgorithmNode> convertNodetoAlgorithmNode(List<Node> nodes) {
        List<AlgorithmNode> algorithmNodeList = new ArrayList<>();

        for (Node node : nodes) {
            algorithmNodeList.add(new AlgorithmNodeImp(node.getName()));
        }

        return algorithmNodeList;
    }

	/**
	 * This method creates a clone/copy of the algorithm node. This allows us to assign cores
	 * to the node representation without affecting the original object.
	 *
	 * @return AlgorithmNode with all the same field values as the original
	 */
	AlgorithmNode createClone();

	/**
	 * Get the name of the node so we can lookup the node in DAG.
	 *
	 * @return string name of the node
	 */
	String getNodeName();

	/**
	 * Return the core number the node has been assigned
	 *
	 * @return core number
	 */
	int getCore();

	/**
	 * Set the core of the node
	 *
	 * @param core - represents which core assigned
	 */
	void setCore(int core);
}
