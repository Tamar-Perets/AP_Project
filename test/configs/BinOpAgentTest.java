package configs;  // TODO: for project, change package name to configs

import org.junit.Test;

import agentsForTests.ResultCatcher;
import graph.Message;
import graph.Topic;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;

import org.junit.Before;
import static org.junit.Assert.*;



public class BinOpAgentTest {

    @Before
    public void setUp() {
        TopicManagerSingleton.get().clear(); 
    }
    
    @Test
    public void testAdditionAgent() {
    	TopicManager tm = TopicManagerSingleton.get();
        Topic topicA = tm.getTopic("A");
        Topic topicB = tm.getTopic("B");
        Topic topicC = tm.getTopic("C");
    	
        BinOpAgent plusAgent = new BinOpAgent("PlusAgent", "A", "B", "C", (x, y) -> x + y);
        
        // create the helper agent and register it to the outTopic af plusAgent (so he will get resluts)
        ResultCatcher catcher = new ResultCatcher();
        topicC.subscribe(catcher);

        // update only one input, expression should not run yet
        topicA.publish(new Message(5.0));
        assertTrue("Agent should wait for both inputs before publishing", Double.isNaN(catcher.getLastResult()));

        // Update both inputs, check the result at catcher
        topicB.publish(new Message(3.0));
        assertEquals("5 + 3 should be 8", 8.0, catcher.getLastResult(), 0.001);
        
        // callback with wrong topic
        plusAgent.callback("WrongTopic", new Message(100.0));
        assertEquals("Should ignore wrong topics", 8.0, catcher.getLastResult(), 0.001);
        
        // Reactivity check
        topicA.publish(new Message(10.0));
        assertEquals("10 + 3 should be 13", 13.0, catcher.getLastResult(), 0.001);
               
        // one input is NaN again
        topicA.publish(new Message("hi"));
        assertEquals("NaN should not cause a change", 13.0, catcher.getLastResult(), 0.001);
        
        // two inputs are NaN
        topicB.publish(new Message("hi2"));
        assertEquals("NaN should not cause a change", 13.0, catcher.getLastResult(), 0.001);
        
        // check reset dont publish, and that it is work
        catcher.reset();
        plusAgent.reset();
        assertTrue("Reset should not publish anything", Double.isNaN(catcher.getLastResult()));
        topicA.publish(new Message(10.0));
        assertEquals("After reset, missing inputs act as 0", 10.0, catcher.getLastResult(), 0.001);
        
        // create agent with null lambda expression
        assertThrows(NullPointerException.class, () -> {
            BinOpAgent badAgent = new BinOpAgent("Bad", "A", "B", "C", null);
            badAgent.callback("A", new Message(1.0));
            badAgent.callback("B", new Message(1.0));
        });
        
    }

}
