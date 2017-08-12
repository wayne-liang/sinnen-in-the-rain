package implementations.algorithm;

import interfaces.algorithm.AlgorithmNode;

/**
 * This class stores a representation of a node and core from the
 * algorithim class. This allows us to check for validity and calculate
 * time to run without extra baggage of the node class.
 */
public class AlgorithmNodeImp implements AlgorithmNode {
	final private String _nodeName;
	private int _core;

	public AlgorithmNodeImp(String nodeName) {
		_nodeName = nodeName;
	}

	/**
	 * Create a copy of the AlgorithmNodeImp object
	 *
	 * @return a copy of the AlgorithmNodeImp
	 */
	@Override
	public AlgorithmNodeImp createClone() {
		return new AlgorithmNodeImp(_nodeName);
	}

	@Override
	public String getNodeName() {
		return _nodeName;
	}

	@Override
	public int getCore() {
		return _core;
	}

	@Override
	public void setCore(int core) {
		_core = core;
	}
}
