package graph;
// TODO change from prints to Assert

import org.junit.Test;

import agentsForTests.tstAgent1;

public class ParallelAgentTest {
	
	// this method tests:
	// 1. creating object, adding tasks to the queue
	// 2. the creation of new thread which run the tasks
	// 3. calling close, the close of that thread
	@Test 
	public void testBasic() {
		tstAgent1 agent = new tstAgent1("agent 1");
		ParallelAgent pagent = new ParallelAgent(agent, 3);
		
		// 1. --- test getName()
		if (!pagent.getName().equals("agent 1"))
			System.out.println("error with getName method");
		
		Message msg1 = new Message("this is msg1");
		Message msg2 = new Message("this is msg2");	
		Message stopMsg1 = new Message(1);
		
		// 2. --- test thread creation
		// thread before any callback - the original thread
		String originalThread = agent.getExeThreadName();
		pagent.callback("topic 1", msg1);
		// let the thread be opened
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String thread1 = agent.getExeThreadName();
		if (originalThread.equals(thread1))
			System.out.println("error, no new thread opened");
		// add task
		pagent.callback("topic 2", msg2);
		// no new thread should be opened
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String thread2 = agent.getExeThreadName();
		if (!thread2.equals(thread1))
			System.out.println("error, new thread is opened for every task");
		
		// 3. --- test tasks handle
		if (agent.getSum() != 2)
			System.out.println("error, not all tasks done");
		
		// 4. --- test queue capasity
		pagent.callback("stop", stopMsg1);
		pagent.callback("stop", stopMsg1);
		pagent.callback("stop", stopMsg1);
		pagent.callback("stop", stopMsg1);
		pagent.callback("stop", stopMsg1);
		if (pagent.getQueueSize() != 3)
			System.out.println("error with tasks queue size");
		
		// 5. --- test reset queue
		pagent.reset();
		if (pagent.getQueueSize() != 0)
			System.out.println("error with reset tasks queue");
		
		// 6. --- test close() (that the threads are closed)
		pagent.close();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (pagent.thread.isAlive())
			System.out.println("error in close() with the thread interupting");
	}
	
}
