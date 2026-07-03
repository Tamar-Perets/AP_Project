package servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;

import server.RequestParser.RequestInfo;
import views.HtmlGraphWriter;
import configs.GenericConfig;
import configs.Graph;

/**
 * A servlet responsible for loading computational graph configurations.
 *
 * <p>This servlet receives a configuration file uploaded by the user,
 * extracts the configuration content, saves it temporarily on the server,
 * loads it using {@link configs.GenericConfig}, builds the corresponding
 * computational graph, and returns an HTML visualization of the graph.</p>
 *
 * <p>The generated graph is displayed in the application's central panel.</p>
 *
 * @author Ofek Sharon
 * @author Tamar Perets
 */
public class ConfLoader implements Servlet {

    /**
     * Handles an uploaded configuration file.
     *
     * <p>The uploaded configuration is extracted from the multipart HTTP
     * request, written to a temporary file, loaded into the system,
     * converted into a graph representation, and returned to the client
     * as an HTML page.</p>
     *
     * @param ri the parsed HTTP request containing the uploaded configuration
     * @param toClient the output stream used to send the HTML response
     * @throws IOException if reading the uploaded file or writing the response fails
     */
    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {

        byte[] rawContent = ri.getContent();

        if (rawContent == null || rawContent.length == 0) {
            sendError(toClient, 400, "Bad Request: No file content found.");
            return;
        }

        try {
            String rawBody = new String(rawContent, StandardCharsets.UTF_8);
            String fileContent = extractConfigContent(rawBody);

            Path configDir = Paths.get("configs");
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            Path tempConfFile = Paths.get("configs", "uploaded_conf.txt");
            Files.write(tempConfFile, fileContent.getBytes(StandardCharsets.UTF_8));

            GenericConfig config = new GenericConfig();
            config.setConfFile(tempConfFile.toString());
            config.create();

            Graph graph = new Graph();
            graph.createFromTopics();

            List<String> htmlLines = HtmlGraphWriter.getGraphHTML(graph, "html_files");

            StringBuilder htmlBody = new StringBuilder();
            for (String line : htmlLines) {
                htmlBody.append(line).append("\n");
            }

            sendHtml(toClient, htmlBody.toString());

        } catch (Exception e) {
            System.err.println("Error loading configuration: " + e.getMessage());
            e.printStackTrace();
            sendError(toClient, 500, "Internal Server Error: Could not process configuration.");
        }
    }

    /**
     * Extracts the configuration text from a multipart/form-data HTTP request.
     *
     * <p>The method removes multipart boundaries and HTTP headers, returning
     * only the actual configuration file content.</p>
     *
     * @param rawBody the raw multipart request body
     * @return the extracted configuration text
     */
    private String extractConfigContent(String rawBody) {
        String normalized = rawBody.replace("\r\n", "\n");

        String[] lines = normalized.split("\n");
        List<String> cleanLines = new ArrayList<>();

        boolean insideFileContent = false;

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.startsWith("--")) {
                if (insideFileContent) {
                    break;
                }
                continue;
            }

            if (trimmed.toLowerCase().startsWith("content-disposition:")) {
                insideFileContent = false;
                continue;
            }

            if (trimmed.toLowerCase().startsWith("content-type:")) {
                continue;
            }

            if (trimmed.isEmpty()) {
                if (!insideFileContent) {
                    insideFileContent = true;
                }
                continue;
            }

            if (insideFileContent) {
                cleanLines.add(line.trim());
            }
        }

        if (cleanLines.isEmpty()) {
            return rawBody.trim();
        }

        return String.join("\n", cleanLines);
    }

    /**
     * Sends a complete HTTP response containing an HTML document.
     *
     * @param toClient the client output stream
     * @param html the HTML document to send
     * @throws IOException if writing to the client fails
     */
    private void sendHtml(OutputStream toClient, String html) throws IOException {
        byte[] bodyBytes = html.getBytes(StandardCharsets.UTF_8);

        String header =
                "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html; charset=UTF-8\r\n" +
                "Content-Length: " + bodyBytes.length + "\r\n" +
                "\r\n";

        toClient.write(header.getBytes(StandardCharsets.UTF_8));
        toClient.write(bodyBytes);
        toClient.flush();
    }

    /**
     * Sends an HTTP error response to the client.
     *
     * @param toClient the client output stream
     * @param statusCode the HTTP status code
     * @param message the error message displayed in the HTML page
     * @throws IOException if writing to the client fails
     */
    private void sendError(OutputStream toClient, int statusCode, String message) throws IOException {
        String html =
                "<html><body>" +
                "<h2>Error " + statusCode + "</h2>" +
                "<p>" + message + "</p>" +
                "</body></html>";

        sendHtml(toClient, html);
    }

    /**
     * Releases resources associated with this servlet.
     *
     * <p>This implementation does not allocate external resources,
     * therefore no cleanup is required.</p>
     *
     * @throws IOException never thrown by this implementation
     */
    @Override
    public void close() throws IOException {
    }
}
