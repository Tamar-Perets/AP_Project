package servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import graph.Message;
import graph.Topic;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;
import server.RequestParser.RequestInfo;
/**
 * A servlet responsible for publishing messages to topics and displaying
 * the current values of all topics.
 *
 * <p>When a publish request is received, the servlet extracts the topic
 * name and message from the request parameters, publishes the message,
 * and returns an HTML table containing all topics and their latest values.</p>
 *
 * <p>This servlet is used by the web interface to update the values table
 * shown on the right side of the application.</p>
 *
 * @author Ofek Sharon
 * @author Tamar Perets
 */
public class TopicDisplayer implements Servlet {
	/**
	 * Handles a publish request.
	 *
	 * <p>The request should contain the parameters {@code topicName} and
	 * {@code message}. The message is published to the requested topic,
	 * after which an updated HTML table is generated and returned to the client.</p>
	 *
	 * @param ri the parsed HTTP request
	 * @param toClient the output stream used to send the HTML response
	 * @throws IOException if writing the response fails
	 */
    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        String topicName = ri.getParameters().get("topicName");
        String message = ri.getParameters().get("message");

        if (topicName != null && message != null) {
            TopicManagerSingleton.get()
                    .getTopic(topicName)
                    .publish(new Message(message));
        }

        String html = buildTopicsTableHtml();
        writeHttpResponse(toClient, html);
    }
    /**
     * Generates an HTML page containing the current values of all topics.
     *
     * @return an HTML document representing the topics table
     */
    private String buildTopicsTableHtml() {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; padding: 10px; }");
        html.append("h3 { text-align: center; }");
        html.append("table { width: 100%; border-collapse: collapse; font-size: 14px; }");
        html.append("th, td { border: 1px solid #ccc; padding: 8px; text-align: center; }");
        html.append("th { background-color: #007bff; color: white; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");

        html.append("<h3>Topics</h3>");
        html.append("<table>");
        html.append("<tr>");
        html.append("<th>Topic</th>");
        html.append("<th>Last Value</th>");
        html.append("</tr>");

        TopicManager tm = TopicManagerSingleton.get();

        for (Topic topic : tm.getTopics()) {
            html.append("<tr>");
            html.append("<td>").append(escapeHtml(topic.name)).append("</td>");
            html.append("<td>").append(escapeHtml(getLastValue(topic))).append("</td>");
            html.append("</tr>");
        }

        html.append("</table>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }
    /**
     * Returns the latest value published to the given topic.
     *
     * @param topic the requested topic
     * @return the last published value, or an empty string if no value exists
     */
    private String getLastValue(Topic topic) {
        Message msg = topic.getLastMessage();

        if (msg == null) {
            return "";
        }

        return msg.asText;
    }
    /**
     * Sends a complete HTTP response containing the generated HTML page.
     *
     * @param toClient the client output stream
     * @param body the HTML body to send
     * @throws IOException if writing to the client fails
     */
    private void writeHttpResponse(OutputStream toClient, String body) throws IOException {
        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

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
     * Escapes special HTML characters in the given text.
     *
     * <p>This prevents HTML injection and ensures that topic names and
     * values are displayed correctly in the browser.</p>
     *
     * @param text the text to escape
     * @return the escaped HTML string
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }

        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
    /**
     * Releases servlet resources.
     *
     * <p>This implementation does not allocate resources, therefore no action
     * is required.</p>
     *
     * @throws IOException never thrown by this implementation
     */
    @Override
    public void close() throws IOException {
    }
}