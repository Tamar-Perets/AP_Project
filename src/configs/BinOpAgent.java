package configs; 

import java.util.function.BinaryOperator;

import graph.Agent;
import graph.Message;
import graph.Topic;
import graph.TopicManagerSingleton;

public class BinOpAgent implements Agent {
	private String name;
	private Topic topic1, topic2;	// Topics from which we get inputs
	private double inputTopic1, inputTopic2; // The inputs from the topics
	private Topic outTopic;		// topic to sent the result to
	private BinaryOperator<Double> operation;	// The operations to do on the inputs
	
	// Constructor
	// assuming the topics exist and that the object exist from now on
	public BinOpAgent(String name, String topic1name, String topic2name, String outTopicName, BinaryOperator<Double> operation) {
		this.name = name;
		this.topic1 = TopicManagerSingleton.get().getTopic(topic1name);
		this.topic2 = TopicManagerSingleton.get().getTopic(topic2name);
		this.outTopic = TopicManagerSingleton.get().getTopic(outTopicName);
		this.operation = operation;
		
		// initiate inputes
		this.inputTopic1 = Double.NaN;
		this.inputTopic2 = Double.NaN;
		
		// subscribe to the input topics
		
		this.topic1.subscribe(this);
	    this.topic2.subscribe(this);
	    
	    // add this agent to the publishers of resultTopic
	    this.outTopic.addPublisher(this);
	}
	
	// getters
	@Override
	public String getName() {
		return this.name;
	}
	
	// this method reset the inputs to 0
	@Override
	public void reset() {
		this.inputTopic1 = 0.0;
		this.inputTopic2 = 0.0;
	}
	
	// this method update the input of the corresponding topic, 
	//	    and, if both inputs are double, calculate the lambda expression and publish to outTopic.
	@Override
	public void callback(String topic, Message msg) {
		// update the coressponding input (by topic)
		if (topic.equals(this.topic1.name)) 
			this.inputTopic1 = msg.asDouble;
		else if (topic.equals(this.topic2.name))
			this.inputTopic2 = msg.asDouble;
		else
			return;
		
		// calculate lambda expression
		if (Double.isNaN(inputTopic1) || Double.isNaN(inputTopic2))
			return;
		Message outMsg = new Message(this.operation.apply(inputTopic1, inputTopic2));
		outTopic.publish(outMsg);
	}

	@Override
	public void close() {}
}
