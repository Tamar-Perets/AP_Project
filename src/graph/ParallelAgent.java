// This class is for handling queue of tasks for some agent (messages that the agent needs to handle)
package graph; // package graph

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ParallelAgent implements Agent{
	
	// Inner class that couple Topic object and Message object
	private class TopicMessagePair{
		public final String topic;
		public final Message message;
		
		public TopicMessagePair(String topic, Message message) {
			this.topic = topic;
			this.message = message;
		}
	}
	
	// memebers
	private Agent agent;
	private BlockingQueue<TopicMessagePair> tasksQueue; 
	Thread thread;	// not private for the test
	
	// contructor
	public ParallelAgent(Agent agent, int capacity) {
		this.agent = agent;
		this.tasksQueue = new ArrayBlockingQueue<TopicMessagePair>(capacity);
		// loop of taking out messages from the queue / for real world- its better to not define the thread in the condtructor, but is another methos (for example start).
		this.thread = new Thread(() -> {
			while(!Thread.currentThread().isInterrupted()) {
				try {
					TopicMessagePair tmp = this.tasksQueue.take();
					this.agent.callback(tmp.topic, tmp.message);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}	
		});
		thread.start();
	}

	@Override
	public String getName() {
		return this.agent.getName();
	}

	@Override
	public void reset() {
		this.agent.reset();
		this.tasksQueue.clear();
	}

	@Override
	public void callback(String topic, Message msg) {
		// Insert msg and topic to the queue
		try {
			tasksQueue.put(new TopicMessagePair(topic, msg));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		if(this.thread != null) {
			this.thread.interrupt();
		}
		this.agent.close();
		this.tasksQueue.clear(); // for cleaning, not neccesary
	}
    
	// for tests
	int getQueueSize() {
	    return this.tasksQueue.size();
	}

}
