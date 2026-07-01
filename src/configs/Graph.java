package configs; // package configs

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import graph.Agent;
import graph.Topic;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;

@SuppressWarnings("serial")
public class Graph extends ArrayList<Node>{
	private TopicManager tm;	
    
	public Graph() {
		tm = TopicManagerSingleton.get();
	}
	
	// this method checks for cycles in the graph
    public boolean hasCycles() {
    	for (Node node : this) 
    		if (node.hasCycles())
    			return true;
        return false;
    }
    
    // this methods create the graph from current topics & agents that are in the TopicManager
    public void createFromTopics(){
    	// clear old version if exidt
    	this.clear();
    	
    	Collection<Topic> topics =	tm.getTopics();
    	Node topicNode;
    	ArrayList<Node> topicNeighbors;
    	HashMap<String, Node> agents = new HashMap<>();
    	
    	for (Topic topic : topics) {
    		// add topic as a node
    		topicNode = new Node("T" + topic.name);
    		this.add(topicNode);
    		
    		// go over the agents of the node, add node and edges
    		topicNeighbors = new ArrayList<Node>();
    		
    		// go over the subscribers
    		for (Agent agent : topic.getSubs()) {
    			String agentNodeName = "A" + agent.getName();
    			// if not exist create node
    			if (!agents.containsKey(agentNodeName)) {
    			    Node newAgentNode = new Node(agentNodeName);
    			    this.add(newAgentNode);
    			    agents.put(agentNodeName, newAgentNode);
    			}
    			Node agentNode = agents.get(agentNodeName);
    			// add to list
    			topicNeighbors.add(agentNode);	
    		}
    		// update topic node's neighbors
    		topicNode.setEdges(topicNeighbors);
    		
    		// go over the publishers
    		for (Agent agent : topic.getPubs()) {
    			String agentNodeName = "A" + agent.getName();
    			// if not exist create node
    			if (!agents.containsKey(agentNodeName)) {
    			    Node newAgentNode = new Node(agentNodeName);
    			    this.add(newAgentNode);
    			    agents.put(agentNodeName, newAgentNode);
    			}
    			// add topic to the node neighbors
    			agents.get(agentNodeName).addEdge(topicNode);
    		}
    	}
    	
    }      
}
