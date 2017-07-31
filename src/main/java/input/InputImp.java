package input;

import java.util.List;

import interfaces.Input;

public class InputImp implements Input{
	private int _nOOfProcessors;
	private List<String[]> _graphData;
	
	/**
	 * The constructor will take arguments directly from the command line.
	 * @param filePath
	 * @param noOfProcessors
	 */
	public InputImp (String filePath, String noOfProcessors){
			
	}

	@Override
	public int getProcessorCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<String[]> getGraphData() {
		// TODO Auto-generated method stub
		return null;
	}

}
