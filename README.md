# Melpody - A Java assistant to melody creation

Giorgio De Luca, 875598 Sound and Acoustic Engineering, Politecnico di Milano


----------------------------------------------------------------------------------------------
Java environment: 

JDK 1.8.0_162, JRE 1.8.0_162

----------------------------------------------------------------------------------------------
Build project:

Use the Maven Build to create a jar file executable directly from command line(jar-with-depen-
dencies, if exist).
The jar file is located in the folder "target" of the workspace in use.

----------------------------------------------------------------------------------------------
Configuration of the environment:

1. Create a folder to contain the project
2. Create a subfolder for the jar file
3. Copy the jar file in the subfolder and the properties file in the main folder

----/
	|-melpody_dir
		|-application_jar
			|jarApplication.jar
		|-midi
*			|harmony.mid
*			|abstract_melody.mid
		|-output
*			|outputfile.mid (the output melody of the execution)
*		|melpody.properties
	|

* = files
|- = folder

----------------------------------------------------------------------------------------------
Execution of the program:

Open the command line in the folder containing the jar file and execute the command 

java -jar nameofthejarfile.jar

**********************************************************************************************
Remember that midi files must be named as described above in the environment configuration.
**********************************************************************************************

----------------------------------------------------------------------------------------------
Properties (file melpody.properties in the main folder of the project)

NOVELTY_THRESHOLD = is the value of the threshold for the activation of the novelty feature;
					the value must be in the interval (0.0;1.0)

IONIAN = 1 to allow the calculation of scale with that mode, 0 to exclude the mode from the 
		calculation(same for other modes);
		
MAJOR = 1 to allow the calculation of scale with this specific interval definitions, 0 to ex-
		clude the type of scale from the calculation (same for other types of scales);

SAME_ROOT = true if the scale selected must have the same root note of the chord, 0 otherwise

----------------------------------------------------------------------------------------------
Log directory:

Log file can be found in the main directory of the program, together with the properties file.

----------------------------------------------------------------------------------------------
