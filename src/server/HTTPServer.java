package server; // package server

import servlets.Servlet;
/**
 * Defines the public API of a lightweight HTTP server.
 *
 * <p>The server supports dynamic registration of {@link Servlet}
 * objects according to HTTP methods and URI prefixes. Each incoming
 * request is parsed and dispatched to the matching servlet.</p>
 *
 * <p>Example usage:</p>
 *
 * <pre>{@code
 * HTTPServer server = new MyHTTPServer(8080, 5);
 * server.addServlet("GET", "/app/", new HtmlLoader("html_files"));
 * server.addServlet("POST", "/upload", new ConfLoader());
 * server.addServlet("GET", "/publish", new TopicDisplayer());
 * server.start();
 * }</pre>
 *
 * @author Ofek Sharon
 * @author Tamar Perets
 */
public interface HTTPServer extends Runnable{
    /**
 * Registers a servlet for a specific HTTP method and URI prefix.
 *
 * @param httpCommanmd the HTTP method, such as GET, POST or DELETE
 * @param uri the URI prefix handled by the servlet
 * @param s the servlet instance that handles matching requests
 */
    public void addServlet(String httpCommanmd, String uri, Servlet s);
    /**
 * Removes a servlet registration for the given HTTP method and URI prefix.
 *
 * @param httpCommanmd the HTTP method
 * @param uri the URI prefix to remove
 */
    public void removeServlet(String httpCommanmd, String uri);
    /**
 * Starts the server in a separate thread.
 */
    public void start();
    /**
 * Stops the server and releases its resources.
 */
    public void close();
}
