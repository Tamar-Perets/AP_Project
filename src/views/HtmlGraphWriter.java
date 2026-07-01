package views;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import configs.Graph;

public class HtmlGraphWriter {
	
	// this method get graph and return list of strings that are html describe of the graph
    public static List<String> getGraphHTML(Graph graph) {
        List<String> outputHtml = new ArrayList<>();
        
        // path to graph file 
        String templatePath = "html_files/graph.html";

        try {
            // טעינת כל שורות קובץ ה-HTML לרשימה
            List<String> lines = Files.readAllLines(Paths.get(templatePath));

            // בניית המחרוזות הדינמיות מתוך אובייקט הגרף
            String dynamicNodes = buildNodesJS(graph);
            String dynamicEdges = buildEdgesJS(graph);

            // מעבר על שורות הקובץ והחלפת הסמנים במידע האמיתי
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
        
        /* * TODO: Iterate over your Graph's Topics and Agents.
         * For each Topic, append a string like this:
         * sb.append("{ id: '").append(topicName).append("', label: '").append(topicName).append("', shape: 'box', color: '#a2d5f2' },\n");
         * * For each Agent, append a string like this:
         * sb.append("{ id: '").append(agentName).append("', label: '").append(agentName).append("', shape: 'circle', color: '#ffb3ba' },\n");
         */
         
        return sb.toString();
    }

    private static String buildEdgesJS(Graph graph) {
         StringBuilder sb = new StringBuilder();
         
         /*
          * TODO: Iterate over your Graph's dependencies/edges.
          * For each edge, append a string representing the arrow:
          * sb.append("{ from: '").append(sourceName).append("', to: '").append(targetName).append("', arrows: 'to' },\n");
          */
          
         return sb.toString();
    }
}