package implementations.algorithm;

import interfaces.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * This class stores a representation of a node and core from the
 * algorithim class. This allows us to check for validity and calculate
 * time to run without extra baggage of the node class.
 */
public class AlgorithmNode {
	final private String _nodeName;
	private int _core;

	public AlgorithmNode(String nodeName) {
		_nodeName = nodeName;
	}

	/**
	 * This static method converts a list of nodes to a list of algorithmNode objects
	 *
	 * @param nodes
	 * @return a list of algorithmNode objects.
	 */
	public static List<AlgorithmNode> convertNodetoAlgorithimNode(List<Node> nodes) {
		List<AlgorithmNode> algorithmNodeList = new ArrayList<>();

		for (Node node : nodes) {
			algorithmNodeList.add(new AlgorithmNode(node.getName()));
		}

		return algorithmNodeList;
	}

	/**
	 * Create a copy of the AlgorithmNode object
	 *
	 * @return a copy of the AlgorithmNode
	 */
	public AlgorithmNode createClone() {
		AlgorithmNode node = new AlgorithmNode(_nodeName);
		node.setCore(_core);

		return node;
	}

	public String getNodeName() {
		return _nodeName;
	}

	public int getCore() {
		return _core;
	}

	public void setCore(int core) {
		_core = core;
	}
}
