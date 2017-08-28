import implementations.algorithm.AlgorithmImp;
import implementations.io.Conversion;
import implementations.io.InputImp;
import implementations.io.OutputImp;
import interfaces.algorithm.Algorithm;
import interfaces.io.Input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
	public static void main(String args[]) {
		// these for for future use, not used at the moment
		boolean visualisation = false;
		boolean outputSpec = false;
		String outputFileName= "";
		int noOfParallerCores = 1;
		
		//java jar scheduler.jar INPUT.dot P [OPTION]
		//Optional :
		//-p N
		//-V
		//-o OUTPUT

		//convert to ArrayList
		List<String> argsList = new ArrayList<>(Arrays.asList(args));
		
		// check filepath of .dot file
		final String filePath = argsList.get(0);
		
		if (!filePath.contains(".dot")) {
			throw new IllegalArgumentException("filePath doesn't contain .dot file.");
		}

		//check no. Of Processors is a valid integer
		final String noOfProcessors = argsList.get(1);
		try {
			//test if it is an int
			Integer.parseInt(noOfProcessors);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("no Of Processors not a valid integer");
		}

		//optional options
		//hard-coded for now
		if (argsList.size() > 2) {
			for (int i = 2; i < argsList.size(); i++) {
				String str = argsList.get(i);

				if (str.contains("-v")) {
					visualisation = true;
				} else if (str.contains("-p")) {

					String tempInt = argsList.get(i + 1);
					
					try {
						// test if it is an int
						noOfParallerCores = Integer.parseInt(tempInt);
					} catch (NumberFormatException e) {
						throw new IllegalArgumentException("no of parallel cores not a valid integer");
					}

				} else if (str.contains("-o")) {
					outputFileName = argsList.get(i + 1);
					outputSpec = true;
				}
			}
		}

		Input input = new InputImp(filePath, noOfProcessors);

		Conversion conversion = new Conversion(input);

		Algorithm alg = new AlgorithmImp(input.getProcessorCount(),visualisation,noOfParallerCores);

		OutputImp outputImp;

		if (outputSpec){
			outputImp = new OutputImp(alg.getCurrentBestSchedule(), filePath, outputFileName);
		} else {
			outputImp = new OutputImp(alg.getCurrentBestSchedule(), filePath);
		}
	
		outputImp.outputToFile();
	}
}