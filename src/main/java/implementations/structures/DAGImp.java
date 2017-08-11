package implementations.structures;

import interfaces.structures.DAG;
import interfaces.structures.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class is an implementation of the DAG interface.
 * It represents a Directed Acyclic Graph.
 */
public class DAGImp implements DAG{

    /**
     * HashMapList object containing all the Node objects currently held in this DAG.
     */
    private HashMap<String, Node> _nodes;

    /**
     * ArrayList object containing all the starting Node objects.
     * Start nodes are nodes with no incoming arcs, and therefore are reachable at the
     * start of processing the DAG.
     */
    private ArrayList<Node> _startNodes;

    /**
     * Creates a new, empty DAGImp object.
     */
    public DAGImp() {
        _nodes = new HashMap<>();
        _startNodes = new ArrayList<>();
    }

    /**
     * Adds a new {@code Node} onto this graph.
     * @param newNode - {@code Node} object to add to the graph
     */
    @Override
    public void add(Node newNode) {
        _nodes.put(newNode.getName(), newNode);
    }

    /**
     * Returns a {@code List<Node>} object containing all the {@code Nodes} currently stored in this graph.
     * @return {@code List<Node>} of all held {@code Node} objects
     */
    @Override
    public List<Node> getAllNodes() {
        return new ArrayList<>(_nodes.values());
    }

    /**
     * Searches through the DAG for a Node with the given name, returning that node if found.
     * If none is found, null is returned.
     * @param name - name of the node to search for
     * @return {@code Node} with the corresponding name
     */
    @Override
    public Node getNodeByName(String name) {
        return _nodes.get(name);
    }

    /**
     * Adds the {@code Node} objects in the given {@code List<Node>} as starting nodes.
     * @param startNodes - {@code List<Node>} of all starting nodes to add
     */
    @Override
    public void addStartNodes(List<Node> startNodes) {
        _startNodes.addAll(startNodes);
    }

    @Override
    public List<Node> getStartNodes() {
        return _startNodes;
    }
}
