package configs;	

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import graph.Agent;
import graph.ParallelAgent;

import java.io.IOException;
import java.lang.reflect.Constructor;
/**
 * Loads a computational configuration from a text file.
 *
 * <p>The configuration file defines agents using class names,
 * subscribed topics and published topics. Agents are created dynamically
 * using Java Reflection.</p>
 */
public class GenericConfig implements Config {
	
	private String confFile;
	private List<Agent> agentsList;
	
	// constructor
	public GenericConfig() {
		this.confFile = null;
		agentsList = new ArrayList<Agent>();
	}
	/**
 * Sets the configuration file path.
 *
 * @param filename path to the configuration file
 */
	// set configuration file name
	public void setConfFile(String confFile) {
		this.confFile = confFile;
	}
	/**
 * Reads the configuration file and creates the defined agents.
 */
	// this method create the agents that are define in file confFile
	//		each agent define by 3 lines: 1. full name  2. topic to subscribe to  3. topic to publish to
	@Override
	public void create() {
		// Reads all lines from the file into a List of Strings
		List<String> lines;
		try {
		    lines = Files.readAllLines(Paths.get(this.confFile));
		} catch (IOException e) {
		    System.out.println("Error reading the config file: " + e.getMessage());
		    return;
		}
		
		// Check if the size is divisible by 3
		if (lines.size() % 3 != 0) {
		    throw new IllegalArgumentException("Configuration file format error: Number of lines must be a multiple of 3");
		}
		
		// read each 3 lines to create agents
		for(int i = 0; i < lines.size(); i+=3) {
			// extract subs and pubs
			String[] subs = lines.get(i+1).split(",");
			String[] pubs = lines.get(i+2).split(",");
			
			Agent newAgent;
			try {
			    // extract class from first line
			    Class<?> agentClass = Class.forName(lines.get(i)); 
			    // find corresponding constructor
			    Constructor<?> constructor = agentClass.getConstructor(String[].class, String[].class);
			    // create new agent according to the extracted data
			    newAgent = (Agent) constructor.newInstance((Object) subs, (Object) pubs);
			} catch (ReflectiveOperationException e) {
			    this.close(); 
			    throw new RuntimeException("Error with reflection operation for agent: " + lines.get(i), e);
			} catch (SecurityException | IllegalArgumentException e) {
			    this.close();
			    throw new RuntimeException("Security or argument error for agent: " + lines.get(i), e);
			} catch (ClassCastException e) {
			    this.close();
			    throw new RuntimeException("The given class is not an Agent: " + lines.get(i), e);
			}
			
			// wrap the agent with ParallelAgent and add it to the agent list
			ParallelAgent newParAgent = new ParallelAgent(newAgent, 30); // TODO - what is the right capacity?
			this.agentsList.add(newParAgent);
		}	
	}

	@Override
	public String getName() {
		return "GenericConfig\\" + this.confFile;
	}

	@Override
	public int getVersion() {
		return 1;
	}
/**
 * Closes all agents created by this configuration.
 */
	@Override
	public void close() {
		this.agentsList.forEach(a -> a.close());
	}

}
