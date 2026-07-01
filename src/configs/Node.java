package configs; // TODO: for project, change package name to configs

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import graph.Message;


public class Node {
    private String name;
    private List<Node> edges;	// nodes that the current node has edges to (direct)
    private Message msg;
    
    // constructor
    public Node(String name) {
		this.name = name;
		this.edges = new ArrayList<Node>();  // TODO check if multi-threading should be consider
	}
    
    
    // Getters
	public String getName() {
		return name;
	}
	
	public List<Node> getEdges() {
		return java.util.Collections.unmodifiableList(this.edges);
	}
	
	public Message getMsg() {
		return msg;  // Reminder: Message is immutable
	}
	
	// Setters
	public void setName(String name) {
		this.name = name;
	}
	
	// set to the exact list that recieved
	public void setEdges(List<Node> edges) {
		this.edges = new ArrayList<>(edges); 
	}

	public void setMsg(Message msg) {
		this.msg = msg; 
	}
	
	// this method get Node object and add it to the neighbors list
	public void addEdge(Node node) {
		if (node == null)
			return;
		this.edges.add(node);
	}
	
	// this method return true if the connented component the node belong to has cycle int it
	public boolean hasCycles() {
		Set<Node> visited = new HashSet<>();
		Set<Node> inPath = new HashSet<>();
		return directDFS(this, visited, inPath);
	}
	
	// this is helper function for finding cycles, performing DFS
	private boolean directDFS(Node node, Set<Node> visited, Set<Node> inPath) {
		List<Node> neighbors = node.getEdges();
		inPath.add(node);
	    visited.add(node);
		// go over the neighbord
		for (Node neighbor : neighbors) {
			// chack cycle
	        if (inPath.contains(neighbor)) 
	            return true;
	        // if this is a new node - recursive call
	        if (!visited.contains(neighbor))
		        if (directDFS(neighbor, visited, inPath))
		        	return true;
		}
		inPath.remove(node); 
		return false;
	}	
}