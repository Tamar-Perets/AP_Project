package configs; // TODO: for project, change package name to configs

import org.junit.Before;
import org.junit.Test;

import agentsForTests.ResultCatcher;
import graph.Agent;
import graph.Message;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;

import static org.junit.Assert.*;

public class PlusAndIncAgentsTest {
    private TopicManager tm;

    @Before
    public void setUp() {
        // clear singelton before each test
        TopicManagerSingleton.get().clear();
        tm = TopicManagerSingleton.get();
    }

    @Test
    public void testPlusAgentNormalExecution() {
        String[] subs = {"A", "B"};
        String[] pubs = {"C"};
        
        @SuppressWarnings("unused")
		Agent plusAgent = new PlusAgent(subs, pubs);
        ResultCatcher catcher = new ResultCatcher();
        
        // subscribe catcher to result topic to get the result
        tm.getTopic("C").subscribe(catcher);

        // update first input
        tm.getTopic("A").publish(new Message(5.0));
        assertEquals("5 + 0 should be 5", 5.0, catcher.getLastResult(), 0.001);

        // update second input
        tm.getTopic("B").publish(new Message(3.0));
        assertEquals("5 + 3 should be 8", 8.0, catcher.getLastResult(), 0.001);
    }

    @Test
    public void testPlusAgentEdgeCases() {
        String[] subs = {"X", "Y"};
        String[] pubs = {"Z"};
        
        Agent plusAgent = new PlusAgent(subs, pubs);
        ResultCatcher catcher = new ResultCatcher();
        tm.getTopic("Z").subscribe(catcher);
        
        // publish from unknown topic should not affect
        plusAgent.callback("WrongTopic", new Message(100.0));
        assertTrue("Wrong topic should be ignored", Double.isNaN(catcher.getLastResult()));
        
        // send NaN as input, should not result publish (initiate with some other input first)
        tm.getTopic("X").publish(new Message(1.0));
        tm.getTopic("X").publish(new Message(Double.NaN));
        assertEquals("Publishing NaN should be ignored", 1.0, catcher.getLastResult(), 0.001);

        // check Reset
        plusAgent.reset();
        tm.getTopic("X").publish(new Message(10.0));
        assertEquals("After reset, missing input is 0", 10.0, catcher.getLastResult(), 0.001);
    }

    @Test
    public void testIncAgentNormalExecution() {
        String[] subs = {"Input"};
        String[] pubs = {"Output"};
        
        @SuppressWarnings("unused")
		Agent incAgent = new IncAgent(subs, pubs);
        ResultCatcher catcher = new ResultCatcher();
        tm.getTopic("Output").subscribe(catcher);
        
        // publish new message to incAgent
        tm.getTopic("Input").publish(new Message(7.0));
        assertEquals("7 + 1 should be 8", 8.0, catcher.getLastResult(), 0.001);
        
        // publish new negtive message to incAgent
        tm.getTopic("Input").publish(new Message(-5.0));
        assertEquals("-5 + 1 should be -4", -4.0, catcher.getLastResult(), 0.001);
    }

    @Test
    public void testIncAgentEdgeCases() {
        String[] subs = {"Input"};
        String[] pubs = {"Output"};
        
        Agent incAgent = new IncAgent(subs, pubs);
        ResultCatcher catcher = new ResultCatcher();
        tm.getTopic("Output").subscribe(catcher);
        
        // wrong topic
        incAgent.callback("WrongTopic", new Message(100.0));
        assertTrue("Wrong topic should be ignored", Double.isNaN(catcher.getLastResult()));
    
        // publish NaN to incAgent
        tm.getTopic("Input").publish(new Message(-1.0));
        tm.getTopic("Input").publish(new Message(Double.NaN));
        assertEquals("NaN should be ignored", 0.0, catcher.getLastResult(), 0.001);
    }
        
    
    // make sure the application collapses neatly if the inputs array are too shorts
    @Test(expected = IllegalArgumentException.class)
    public void testPlusAgentShortArraysThrowsException() {
        String[] shortSubs = {"A"};
        String[] pubs = {"C"};
        
        new PlusAgent(shortSubs, pubs);
    }
}