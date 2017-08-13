package implementations.io;

import interfaces.io.Output;
import interfaces.structures.NodeSchedule;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;

public class OutputImp implements Output {
	private HashMap<String, NodeSchedule> _bestSchedule;
	private String _inputPath;
	private String _outputPath;
	public final static String NEWLINE = System.getProperty("line.separator");

	public OutputImp(HashMap<String, NodeSchedule> bestSchedule, String inputPath) {
		this (bestSchedule, inputPath, null);
	}

	public OutputImp(HashMap<String, NodeSchedule> bestSchedule, String inputPath, String outputPath) {
		_bestSchedule = bestSchedule;
		_inputPath = inputPath;
		_outputPath = outputPath;
	}
	
	public void printOutput() {
		File file = new File (_inputPath);
		StringBuilder sb = new StringBuilder();
		
		try {
			Scanner inputScanner = new Scanner (file);
			
			//Append "output" to the digraph name.
			String firstLine = inputScanner.nextLine(); 
			int qutIndex = firstLine.indexOf("\"");
			sb.append(firstLine.substring(0, qutIndex+1));
			sb.append("output");
			sb.append(firstLine.substring(qutIndex+1, qutIndex+2).toUpperCase());
			sb.append(firstLine.substring(qutIndex+2, firstLine.length()));
			sb.append(NEWLINE);
			
			//Loop to read all lines. Only exit if reaches a single line with closing bracket }.
			while (true) {
				String line = inputScanner.nextLine();
				
				//Final line escape.
				if (line.trim().equals("}")) {
					sb.append(line);
					break;
				}
				
				String[] lineArray = line.split("\\[");
				
				//Processing the name (index 1)
				lineArray[0] = lineArray[0].trim();				
				if (!lineArray[0].contains("->")) {//For a node
					String nameOfNode = lineArray[0].trim();
					NodeSchedule ns = _bestSchedule.get(nameOfNode);
					
					int sqbkIndex = line.indexOf("]");
					sb.append(line.substring(0, sqbkIndex));
					sb.append(",Start=" + ns.getBestStartTime() + ",Processor=" + ns.getBestProcessor());
					sb.append(line.substring(sqbkIndex, line.length()));
					sb.append(NEWLINE);
					
				}
				else { //For an arc
					sb.append(line);
					sb.append(NEWLINE);
				}							
			}
			System.out.println(sb.toString());
			inputScanner.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * TODO: Need to implement write to an output file instead
	 * of printing to console.
	 */
	public void outputToFile(){
		
	}
}
