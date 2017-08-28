# sinnen-in-the-rain
SOFTENG306 Project One

## Team Members
* Daniel Wong (Leader)
* Victor Lian
* Wayne Liang
* Pulkit Kalra
* Darius Au
* Mike Lee

## Git Name Service (GNS)
| Github Username | Full Name | ID | UPI |
| --- | --- | --- | --- |
| dan-wong | Daniel Wong | 137178800 | dwon184 |
| victorlian | Victor Lian | 442213705 | vlia679 |
| TacticalSandwich | Wayne Liang | 780611815 | wlia631 |
| pulkitkalra | Pulkit Kalra | 212806681 | pkal608 |
| dariusau | Darius Au | 399652431 | dau782 |
| st970703 | Mike Lee | 20368209 | elee353 |

## Build Instructions
1. Download this repository or use `git clone` to create a local copy
2. Navigate to the directory containing the pom file and run `mvn install`
    - You can also import the project into your IDE and run Maven from there
3. Navigate to the `target` directory, where you should find `scheduler.jar`

**NOTE**: To compile it yourself, the Parallel Task Runtime (PTRuntime.jar) needs to be installed in your local Maven repo. The jar file can be found here: http://parallel.auckland.ac.nz/ParallelIT/PT_Download.html (Created by Dr. Oliver Sinnen and Dr. Nasser Giacaman). Installation into your local Maven repo:  
>`mvn install:install-file -Dfile=<path-to-PTRuntime.jar> -DgroupId=ParallelTask -DartifactId=PTRuntime -Dversion=1.0.0 -Dpackaging=jar`

## Run Instructions
The JAR file can be run with the command:  
>`java -jar scheduler.jar <path-to-input.dot> P [OPTION]`  

**Optional Note**: To run the User Interface with visual enhancements (icons and images added) - download the images directory at the top of this repository and place it in the same folder as the jar file.

Where:  
    `<path-to-input.dot>` is the path to a task graph in dot format  
    `P` is the number of processors to schedule the input graph on  
    
Optional:  
    `-p N` use N cores for execution in parallel  
    `-v` visualize the search  
    `-o OUTPUT` output file named OUTPUT  
