// This file wrotten by AI (Gemini)
package configs; // TODO: for project, change package name to configs

import org.junit.Before;
import org.junit.Test;

import graph.Agent;
import graph.Message;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;

import static org.junit.Assert.*;


public class GraphTest {

    // Helper agent 
    class DummyAgent implements Agent {
        private String name;

        public DummyAgent(String name, String[] subs, String[] pubs) {
            this.name = name;
            TopicManager tm = TopicManagerSingleton.get();
            // subscribe topics
            for (String sub : subs) {
                tm.getTopic(sub).subscribe(this);
            }
            // add as publisher
            for (String pub : pubs) {
                tm.getTopic(pub).addPublisher(this);
            }
        }

        @Override public String getName() { return this.name; }
        @Override public void reset() {}
        @Override public void callback(String topic, Message msg) {}
        @Override public void close() {}
    }
    
    
    @Before
    public void setUp() {
        // clear singelton for clean environment
        TopicManagerSingleton.get().clear();
    }

    @Test
    public void testNormalExecutionAndNoDuplicates() {
        // environment setting
    	// Agent1 reads from T1 and piblishes to T2
        new DummyAgent("Agent1", new String[]{"T1"}, new String[]{"T2"});
        // Agent2 also reads from T1 and piblishes to T2
        new DummyAgent("Agent2", new String[]{"T1"}, new String[]{"T2"});

        Graph graph = new Graph();
        graph.createFromTopics();

        // check1: count nodes (should be 4) (TT1, TT2, AAgent1, AAgent2)
        assertEquals("Graph should contain exactly 4 nodes without duplicates", 4, graph.size());

        // check2: verify node by initiate
        boolean foundT1 = false, foundT2 = false, foundA1 = false;
        for (Node n : graph) {
            if (n.getName().equals("TT1")) foundT1 = true;
            if (n.getName().equals("TT2")) foundT2 = true;
            if (n.getName().equals("AAgent1")) foundA1 = true;
        }
        assertTrue("Node TT1 should exist", foundT1);
        assertTrue("Node TT2 should exist", foundT2);
        assertTrue("Node AAgent1 should exist", foundA1);

        // check3: check for cycles
        assertFalse("Graph should not contain cycles", graph.hasCycles());
    }

    @Test
    public void testGraphRecreationAfterManagerUpdate() {
        // create primary graph
        new DummyAgent("Agent1", new String[]{"T1"}, new String[]{"T2"});
        
        Graph graph = new Graph();
        graph.createFromTopics();
        assertEquals("Initial graph should have 3 nodes", 3, graph.size());

        // add new agent and topic to the exist TopicManager 
        new DummyAgent("Agent2", new String[]{"T2"}, new String[]{"T3"});

        // re-call the creation function
        graph.createFromTopics();
        
        // the nodes: TT1, TT2, TT3, AAgent1, AAgent2
        assertEquals("Graph should have exactly 5 nodes after recreation", 5, graph.size());
    }

    @Test
    public void testGraphWithCycle() {
    	// create (direct) cycle
    	// A1 call fron T1 and pub to T2
    	// A2 call from T2 and pub to T1
        new DummyAgent("Agent1", new String[]{"T1"}, new String[]{"T2"});
        new DummyAgent("Agent2", new String[]{"T2"}, new String[]{"T1"});

        Graph graph = new Graph();
        graph.createFromTopics();

        // check for the cycle detection
        assertTrue("Graph MUST detect a cycle", graph.hasCycles());
    }

    @Test
    public void testEmptyGraphAndSelfLoopEdgeCases() {
        Graph graph = new Graph();
        
        // edge case 1: nothing in tm
        graph.createFromTopics();
        assertEquals("Empty manager should create empty graph", 0, graph.size());
        assertFalse("Empty graph has no cycles", graph.hasCycles());

        // edge case 2: loop of size 2 (agent the sub and pub to the same topic)
        new DummyAgent("SelfLoopAgent", new String[]{"T1"}, new String[]{"T1"});
        graph.createFromTopics();
        
        assertEquals("Graph should have 2 nodes", 2, graph.size());
        assertTrue("Self loop is a cycle", graph.hasCycles());
    }
}