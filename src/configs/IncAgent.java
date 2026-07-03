package configs;

import graph.Agent;
import graph.Message;
import graph.Topic;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;

public class IncAgent implements Agent {
	private String subTopic;
	private Topic pubTopic;
	
	// constructor
		// subscribe only to the first topic in subs
		// piblish only to the first topic in pubs
		// the topics for sub and pub only determine once at the begginning
	public IncAgent(String[] subs, String[] pubs) {
		// input validation
		if (subs.length < 1 || pubs.length < 1) {
		    throw new IllegalArgumentException("IncAgent requires at least 1 sub and 1 pub");
		}
		
		TopicManager tm = TopicManagerSingleton.get();
		
		// subscribe to the first subs topic
		this.subTopic = subs[0];
		tm.getTopic(subTopic).subscribe(this);
		
		// register as publisher to the first pubs topic
		this.pubTopic = tm.getTopic(pubs[0]);
		pubTopic.addPublisher(this);
	}
	
	// Getters
	@Override
	public String getName() { return "IncAgent";	}

	@Override
	public void reset() {}

	@Override
	public void callback(String topic, Message msg) {
		// check that the topic is valid
		if (!topic.equals(subTopic) || Double.isNaN(msg.asDouble))
			return;
		// calculate value++ and publish
		pubTopic.publish(new Message(msg.asDouble + 1));
	}

	@Override
	public void close() {}   
}
