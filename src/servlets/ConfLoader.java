package servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import server.RequestParser.RequestInfo;
import views.HtmlGraphWriter;
import configs.GenericConfig;
import configs.Graph;
/**
 * A servlet responsible for loading computational configuration files.
 *
 * <p>The servlet handles configuration upload requests. It extracts the
 * uploaded file name and content from the request, saves the configuration
 * on the server side, loads it using {@link GenericConfig}, builds a
 * {@link Graph} from the resulting topics and agents, and returns an HTML
 * visualization of the graph.</p>
 *
 * <p>The graph visualization is delegated to {@link HtmlGraphWriter}.</p>
 */
public class ConfLoader implements Servlet {
/**
 * Handles a configuration upload request.
 *
 * <p>The method saves the uploaded configuration, creates the computational
 * network, builds a graph from the current topics and agents, and writes an
 * HTML response containing the graph visualization.</p>
 *
 * @param ri parsed request information containing the uploaded content
 * @param toClient stream used to send the generated graph HTML
 * @throws IOException if saving the file or writing the response fails
 */
    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        
        byte[] rawContent = ri.getContent();
        
        // Handle empty requests safely
        if (rawContent == null || rawContent.length == 0) {
            sendError(toClient, 400, "Bad Request: No file content found.");
            return;
        }

        // 1. Extract the pure text from the HTTP multipart form data
        String rawBody = new String(rawContent);
        String fileContent = extractFileText(rawBody);

        try {
            // 2. Safely create the "configs" directory if it doesn't exist
            Path configDir = Paths.get("configs");
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
            
            // 3. Save the uploaded text to a temporary physical file
            Path tempConfFile = Paths.get("configs", "uploaded_conf.txt");
            Files.write(tempConfFile, fileContent.getBytes());

            // 4. Initialize GenericConfig with the new file
            GenericConfig config = new GenericConfig();
            config.setConfFile(tempConfFile.toString());
            config.create(); // Creates agents and populates the TopicManagerSingleton
            
            // 5. Build the Graph from the Singleton data
            Graph myGraph = new Graph();
            myGraph.createFromTopics();

            // 6. Generate the HTML view using your HtmlGraphWriter
            List<String> htmlResponseLines = HtmlGraphWriter.getGraphHTML(myGraph, "html_files");
            
            StringBuilder htmlBody = new StringBuilder();
            for (String line : htmlResponseLines) {
                htmlBody.append(line).append("\n");
            }
            String finalHtml = htmlBody.toString();

            // 7. Send the HTTP 200 OK Response
            String header = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/html\r\n" +
                            "Content-Length: " + finalHtml.getBytes().length + "\r\n\r\n";
                            
            toClient.write(header.getBytes());
            toClient.write(finalHtml.getBytes());
            toClient.flush();

        } catch (Exception e) {
            System.err.println("Error loading configuration: " + e.getMessage());
            e.printStackTrace(); // Helpful for debugging
            sendError(toClient, 500, "Internal Server Error: Could not process configuration.");
        }
    }
/**
 * Closes the current configuration if one was loaded.
 *
 * @throws IOException never thrown directly by this implementation
 */
    @Override
    public void close() throws IOException {}

    /**
     * A robust method to strip multipart/form-data HTTP boundaries.
     * It dynamically finds the boundary string and reads line-by-line
     * to guarantee headers are ignored.
     */
    private String extractFileText(String rawBody) {
        // Split the string into lines, handling BOTH Windows (\r\n) and Unix (\n)
        String[] lines = rawBody.split("\\r?\\n");
        
        // If it's too short, it's not a multipart form
        if (lines.length < 4) {
            return rawBody; 
        }

        // The first line of the request is ALWAYS the browser's generated boundary
        String boundary = lines[0]; 
        
        // If it doesn't start with "--", it's not multipart, return as is
        if (!boundary.startsWith("--")) {
            return rawBody; 
        }

        int contentStartLine = -1;
        
        // Loop to find the empty line that separates headers from the actual file content
        for (int i = 1; i < lines.length; i++) {
            if (lines[i].trim().isEmpty()) {
                contentStartLine = i + 1; // The file content starts on the next line
                break;
            }
        }

        // If we never found an empty line, something is wrong, return raw
        if (contentStartLine == -1) {
            return rawBody;
        }

        // Read the actual file content until we hit the bottom boundary
        StringBuilder cleanContent = new StringBuilder();
        for (int i = contentStartLine; i < lines.length; i++) {
            if (lines[i].startsWith(boundary)) {
                break; // Stop reading! We hit the bottom WebKit boundary
            }
            cleanContent.append(lines[i]).append("\n");
        }

        // Return the clean string, trimming off any extra trailing whitespace/newlines
        return cleanContent.toString().trim();
    }
    
    /**
     * Helper method to cleanly send HTTP errors back to the browser.
     */
    private void sendError(OutputStream toClient, int statusCode, String message) throws IOException {
        String html = "<html><body><h2>Error " + statusCode + "</h2><p>" + message + "</p></body></html>";
        String header = "HTTP/1.1 " + statusCode + " Error\r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: " + html.getBytes().length + "\r\n\r\n";
        toClient.write(header.getBytes());
        toClient.write(html.getBytes());
        toClient.flush();
    }
}
