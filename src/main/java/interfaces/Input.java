package interfaces;

import java.util.List;

public interface Input {

	/**
	 * Returns the number of processors given to the scheduler.
	 * @return {@code integer} representing the number of processors available
	 */
	public int getProcessorCount();
	
	/**
	 * Returns a {@code List<String[]>} object containing the parsed input graph data.
	 * 
	 * Each {@code String} array in the {@code List} object contains two objects: 
	 * <p>
	 * [0] - node label or arc label
	 * <br>
	 * [1] - weight of the defined node/arc
	 * 
	 * @return {@code List<String[]} object containing the parsed input graph data
	 */
	public List<String[]> getGraphData();
	
}
