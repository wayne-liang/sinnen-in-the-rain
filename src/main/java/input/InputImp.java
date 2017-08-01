package input;

import java.io.BufferedInputStream;
import java.util.List;
import java.util.Scanner;

import interfaces.Input;

/**
 * This class is in the module responsible for taking input files and 
 * do some basic pre-processing.
 *  
 * It will implement the Input interface for the conversion module to
 * convert the input into a DAG.
 * @author Victor
 *
 */
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
	
	private void inputProcess(){
		Scanner inputScanner = new Scanner (new BufferedInputStream(System.in));
	}

}
