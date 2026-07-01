package servlets;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import server.RequestParser.RequestInfo;

public class HtmlLoader implements Servlet {

    private final String baseDirectory;

    // The constructor takes the folder name so it isn't hard-coded
    public HtmlLoader(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        
        // Extract html file name from the request
        String[] segments = ri.getUriSegments();
        String fileName = "index.html"; // default
        
        if (segments != null && segments.length > 0) {
            // take the last segment
            fileName = segments[segments.length - 1];
            
            // check if the client only accessed to /app/ or TODO
            if (fileName.equals("app") || fileName.isEmpty()) {
                fileName = "index.html";
            }
        }
        
        // Build the path
        Path filePath = Paths.get(baseDirectory, fileName);
        File file = filePath.toFile();

        // Read the file and send response back to the client
        if (file.exists() && !file.isDirectory()) {
            byte[] fileContent = Files.readAllBytes(filePath);
            
            // HTTP 200 response (all valid)
            String header = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/html\r\n" +
                            "Content-Length: " + fileContent.length + "\r\n\r\n";
                            
            toClient.write(header.getBytes());
            toClient.write(fileContent);
            
        } else {
            // HTTP 404 response (file not found)
            String errorMsg = "<html><body><h1>404 - File Not Found</h1><p>We couldn't find the file: " + fileName + "</p></body></html>";
            String header = "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Type: text/html\r\n" +
                            "Content-Length: " + errorMsg.length() + "\r\n\r\n";
                            
            toClient.write(header.getBytes());
            toClient.write(errorMsg.getBytes());
        }
        
        toClient.flush();
    }

    @Override
    public void close() throws IOException { }
    
}