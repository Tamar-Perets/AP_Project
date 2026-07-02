package servlets; // package servlets

import java.io.IOException;
import java.io.OutputStream;

import server.RequestParser.RequestInfo;
/**
 * Represents a component that handles a parsed HTTP request.
 *
 * <p>A servlet receives a {@link RequestParser.RequestInfo} object
 * and writes an HTTP response to the provided output stream.</p>
 *
 * <p>Servlet implementations can perform different actions, such as
 * loading HTML files, uploading configurations or publishing messages
 * to topics.</p>
 *
 * @author Ofek Sharon
 * @author Tamar Perets
 */
public interface Servlet {
    /**
 * Handles an incoming HTTP request.
 *
 * @param ri parsed information about the HTTP request
 * @param toClient output stream used to write the HTTP response
 * @throws IOException if writing to the client fails
 */
    void handle(RequestInfo ri, OutputStream toClient) throws IOException;
    /**
 * Releases resources used by the servlet.
 *
 * @throws IOException if closing resources fails
 */
    void close() throws IOException;
}
