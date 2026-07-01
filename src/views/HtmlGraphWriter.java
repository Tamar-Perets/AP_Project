package views;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import configs.Graph;
import configs.Node;

public class HtmlGraphWriter {
	
	// this method get graph and return list of strings that are html describe of the graph
	// i.e generates an HTML representation of the provided computational graph
    public static List<String> getGraphHTML(Graph graph, String templateDir) {
        List<String> outputHtml = new ArrayList<>();
        
        // path to graph file 
        Path templatePath = Paths.get(templateDir, "graph.html");

        try {
            // load all html file lines into a list
        	List<String> lines = Files.readAllLines(templatePath);

            // build dynamic string from the graph
            String dynamicNodes = buildNodesJS(graph);
            String dynamicEdges = buildEdgesJS(graph);

            // go over the file line's, replacing the MARKs with the real data
            for (String line : lines) {
                if (line.contains("// DYNAMIC_NODES_HERE")) {
                    outputHtml.add(dynamicNodes);
                } else if (line.contains("// DYNAMIC_EDGES_HERE")) {
                    outputHtml.add(dynamicEdges);
                } else {
                    outputHtml.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading graph.html template: " + e.getMessage());
            outputHtml.add("<html><body><h2>Error loading graph template</h2></body></html>");
        }

        return outputHtml;
    }

    private static String buildNodesJS(Graph graph) {
        StringBuilder sb = new StringBuilder();
        
        for (Node node: graph) {
        	// seperate node name to type (first letter) and name
        	char type = node.getName().charAt(0);
        	String name = node.getName().substring(1);
        	
        	// For each Topic-node append square node, For each Agent-node append circle node
        	if (type == 'T')
        		sb.append("{ id: '").append(name).append("', label: '").append(name).append("', shape: 'box', color: '#a2d5f2' },\n");
        	else if (type == 'A')
        		sb.append("{ id: '").append(name).append("', label: '").append(name).append("', shape: 'circle', color: '#ffb3ba' },\n");
        }
        
        return sb.toString();
    }

    private static String buildEdgesJS(Graph graph) {
         StringBuilder sb = new StringBuilder();
         
         // go over the nodes
         for (Node node: graph) {
        	 List<Node> edges = node.getEdges();
        	 String nodeName= node.getName().substring(1);
        	 
        	 //  go over the neighbors, add edge for each neighbor
        	 for (Node neighbor : edges) {
        		 String neighborName= neighbor.getName().substring(1);
        		 sb.append("{ from: '").append(nodeName).append("', to: '").append(neighborName).append("', arrows: 'to' },\n");
        	 }	 
         }     
          
         return sb.toString();
    }
}