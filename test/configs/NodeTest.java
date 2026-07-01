package configs;  // TODO: for project, change package name to configs

import org.junit.Test;

import graph.Message;

import static org.junit.Assert.*; 
import java.util.List;
import java.util.ArrayList;

public class NodeTest {
	
	// the test run on the graph:
	//           H     
	//           \_         
	//     E <--- A <--- B      
	//  _/  \_    \_     \_                              
	//  F --> G    C <--> D    I
	//          
	// checked nodes: A, D, E, H, I. 
	@Test
	public void testCycles() {
		// create nodes
		Node A = new Node("A");
		Node B = new Node("B");
		Node C = new Node("C");
		Node D = new Node("D");
		Node E = new Node("E");
		Node F = new Node("F");
		Node G = new Node("G");
		Node H = new Node("H");
		Node I = new Node("temp");
		Node J = new Node("J");
		
		// test setName
		I.setName("I");
		assertEquals("The name of node I should be \\\"I\\\".", "I", I.getName());
		
		// test setMsg
		Message m = new Message("hello world");
		I.setMsg(m);
		assertEquals("The message of node I should have changed.", m, I.getMsg());
		
		// set edges
		A.setEdges(new ArrayList<Node>(List.of(E,C)));
		B.addEdge(A);
		C.addEdge(D);
		D.setEdges(new ArrayList<Node>(List.of(B, C)));
		E.setEdges(new ArrayList<Node>(List.of(F, G)));
		F.addEdge(G);
		H.addEdge(A);
		J.addEdge(J);
		
		// test hasCycle
		assertTrue("error of cycle recognize (false negative), node is part of the cycle (node A)", A.hasCycles());
		assertTrue("error of cycle recognize (false negative), node is part of two cycles (sizes 4,2) (node D)", D.hasCycles());
		assertFalse("error of cycle recognize (false positive), (node E)", E.hasCycles());
		assertTrue("error of cycle recognize (false negative), node is not part of the cycle (node H)", H.hasCycles());
		assertFalse("error of cycle recognize (false positive), (node I)", I.hasCycles());
		assertTrue("error, self loop not detected", J.hasCycles());
	}
	
	

}
