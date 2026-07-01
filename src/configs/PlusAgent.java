package configs;	// TODO: for project, change package name to configs
	//TODO update path at simple.conf


import graph.Agent;
import graph.Message;
import graph.Topic;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;

public class PlusAgent implements Agent{
	
	private double x, y;
	private String xTopic, yTopic; 
	private Topic outTopic;
	
	// constructor
		// subscribe only to the two first topics in subs
		// piblish only to the first topic in pubs
		// the topics for sub and pub only determine once at the begginning
	public PlusAgent (String[] subs, String[] pubs) {
		// input validation
		if (subs.length < 2 || pubs.length < 1) {
		    throw new IllegalArgumentException("PlusAgent requires at least 2 subs and 1 pub");
		}
		
		TopicManager tm = TopicManagerSingleton.get();
		
		// subscribe two first subs topics
		this.xTopic = subs[0];
		this.yTopic = subs[1];
		tm.getTopic(xTopic).subscribe(this);
		tm.getTopic(yTopic).subscribe(this);
		
		// register as publisher to the first pubs topic
		this.outTopic = tm.getTopic(pubs[0]);
		outTopic.addPublisher(this);
		
		// initiate x, y
		this.x = 0.0;
		this.y = 0.0;
	}
	
	// Getters
	@Override
	public String getName() { return "PlusAgent"; }
	
	// reset x, y to 0
	@Override
	public void reset() {
		this.x = 0.0;
		this.y = 0.0;		
	}

	@Override
	public void callback(String topic, Message msg) {
		// check that the topic is valid
		if (topic.equals(xTopic))
			this.x = msg.asDouble;
		else if (topic.equals(yTopic))
			this.y = msg.asDouble;
		else
			return;
		
		// calculate x + y
		if (Double.isNaN(x) || Double.isNaN(y))
			return;
		outTopic.publish(new Message(x + y));		
	}

	@Override
	public void close() {} 
}
