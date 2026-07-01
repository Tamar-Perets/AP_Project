package agentsForTests;

import java.util.concurrent.TimeUnit;

import graph.Agent;
import graph.Message;

//simple agent class for tests
public class tstAgent1 implements Agent{
	String name;
	

	Double sum;
	String exeThreadName; // for current thread name
	
	public tstAgent1(String name){
		this.name = name;
		this.sum = (double) 0; // the number of tasks the agents did
		this.exeThreadName = Thread.currentThread().getName();
	}
	
	// getters
	public Double getSum() {
		return sum;
	}

	public String getExeThreadName() {
		return exeThreadName;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void reset() {
		this.sum = (double) 0;
	}

	@Override
	// increase sum by 1. if topic = "stop", pause for msg.double seconds.
	public void callback(String topic, Message msg) {
		// updating thread name
		this.exeThreadName = Thread.currentThread().getName();
		// pause for "stop" topic
		if ("stop".equals(topic))
			if (!Double.isNaN(msg.asDouble))
				try {
					TimeUnit.SECONDS.sleep((int)msg.asDouble);
				} catch (InterruptedException e) {
					// e.printStackTrace();
					Thread.currentThread().interrupt();
				}
		// update sum
		sum += 1;
	}

	@Override
	public void close() {}
}
