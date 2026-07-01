package graph; // TODO: for project, change package name to package graph; ?

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Topic {
	public final String name;
	List<Agent> subs;
	List<Agent> pubs;
	
	// constructor (privacy: default (package level only))
	public Topic(String name) {
		this.name = name;
		this.subs = new CopyOnWriteArrayList<>();
		this.pubs = new CopyOnWriteArrayList<>();
	}
	
	// Getters
	public List<Agent> getSubs() {
	    return java.util.Collections.unmodifiableList(this.subs);
	}

	public List<Agent> getPubs() {
	    return java.util.Collections.unmodifiableList(this.pubs);
	}
	
	// Add agent to the subscribers list
	public void subscribe(Agent a) {
		if (!this.subs.contains(a))
			this.subs.add(a);
	}
	
	// Remove agent from the subscribers list
	public void unsubscribe(Agent a) { 
		this.subs.remove(a);
	}
	
	// Activate callback methods of all subs
	public void publish(Message msg) {
		for(int i=0; i<this.subs.size(); i++)
			this.subs.get(i).callback(this.name, msg);
	}
	
	// Add agent to the publishers list
	public void addPublisher(Agent a) {
		if (!this.pubs.contains(a))
			this.pubs.add(a);
	}
	
	// Remove agent from the publishers list
	public void removePublisher(Agent a) { 
		this.pubs.remove(a);
	} 

}
