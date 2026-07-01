/* TODO
 *   THIS IS A PARTIAL CODE
 * TODO
*/
package graph;

import org.junit.Test;

public class TopicTest {
	
	// simple agent class for tests
	public class tstTopAgent1 implements Agent{
		String name;
		Double sum;
		
		tstTopAgent1(String name){
			this.name = name;
			this.sum = (double) 0;
		}
		
		@Override
		// return class name
		public String getName() {
			return name;
		}

		@Override
		public void reset() {}

		@Override
		// add msg to sum if a number, sum-1 else
		public void callback(String topic, Message msg) {
			if (!Double.isNaN(msg.asDouble))
				sum += msg.asDouble;
			else
				sum -= 1;
		}

		@Override
		public void close() {}
		
		public void resetSum() {
			this.sum = 0.0;
		}
	}
	
	// This method tests the basic methods: subscribe | unsubscribe | publish | addPublisher | removePublisher
	@Test
	public void testTopic() {
		// create agents and topics
		tstTopAgent1 a1 = new tstTopAgent1("a1");
		tstTopAgent1 a2 = new tstTopAgent1("a2");
		tstTopAgent1 a3 = new tstTopAgent1("a3");
		Topic t1 = new Topic("t1");
		Topic t2 = new Topic("t1");
		
		// add a1 and a2 to t1 subs
		t1.subscribe(a1);
		t1.subscribe(a2);
		// add a3 to t1 pubs
		t1.addPublisher(a3);
		
		// publish at t1
		
		
		
		
		
	}
	
	
	
	
}
