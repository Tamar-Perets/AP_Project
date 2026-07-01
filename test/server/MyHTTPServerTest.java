package server;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Test;

import server.MyHTTPServer;
import server.RequestParser.RequestInfo;
import servlets.Servlet;

public class MyHTTPServerTest {

    private MyHTTPServer server;
    private int serverPort;

    @After
    public void tearDown() throws Exception {
        if (server != null) {
            server.close();
            server.join(2000);
        }
    }

    // -------------------------
    // Helper methods
    // -------------------------

    private int findFreePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private void startServer(int nThreads) throws Exception {
        serverPort = findFreePort();
        server = new MyHTTPServer(serverPort, nThreads);
        server.start();

        // Give the server a short moment to start listening
        Thread.sleep(100);
    }

    private String sendRawRequest(String request) throws IOException {
        try (Socket socket = new Socket("localhost", serverPort)) {
            socket.setSoTimeout(3000);

            OutputStream out = socket.getOutputStream();
            out.write(request.getBytes(StandardCharsets.UTF_8));
            out.flush();
            socket.shutdownOutput();

            ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();

            try {
                int b;
                while ((b = socket.getInputStream().read()) != -1) {
                    responseBuffer.write(b);
                }
            } catch (IOException e) {
                // If timeout happens, return what was already read.
            }

            return responseBuffer.toString(StandardCharsets.UTF_8.name());
        }
    }

    private static class TextServlet implements Servlet {
        private final String text;

        TextServlet(String text) {
            this.text = text;
        }

        @Override
        public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
            toClient.write(text.getBytes(StandardCharsets.UTF_8));
            toClient.flush();
        }

        @Override
        public void close() throws IOException {
            // Nothing to close in this test servlet
        }
    }

    // -------------------------
    // Tests
    // -------------------------

    @Test
    public void testGetRequestExactServletMatch() throws Exception {
        startServer(2);

        server.addServlet("GET", "/hello", new TextServlet("hello response"));

        String response = sendRawRequest(
                "GET /hello HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n"
        );

        assertTrue(response.contains("hello response"));
    }

    @Test
    public void testQueryParametersArePassedToServlet() throws Exception {
        startServer(2);

        server.addServlet("GET", "/publish", new Servlet() {
            @Override
            public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
                String topic = ri.getParameters().get("topic");
                String message = ri.getParameters().get("message");

                String response = "topic=" + topic + ", message=" + message;
                toClient.write(response.getBytes(StandardCharsets.UTF_8));
                toClient.flush();
            }

            @Override
            public void close() throws IOException {
            }
        });

        String response = sendRawRequest(
                "GET /publish?topic=A&message=5 HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n"
        );

        assertTrue(response.contains("topic=A, message=5"));
    }

    @Test
    public void testServletReceivesFullUriIncludingQueryString() throws Exception {
        startServer(2);

        server.addServlet("GET", "/api/resource", new Servlet() {
            @Override
            public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
                toClient.write(ri.getUri().getBytes(StandardCharsets.UTF_8));
                toClient.flush();
            }

            @Override
            public void close() throws IOException {
            }
        });

        String response = sendRawRequest(
                "GET /api/resource?id=123&name=test HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n"
        );

        assertTrue(response.contains("/api/resource?id=123&name=test"));
    }

    @Test
    public void testLongestPrefixMatchChoosesMostSpecificServlet() throws Exception {
        startServer(2);

        server.addServlet("GET", "/app", new TextServlet("general app"));
        server.addServlet("GET", "/app/download", new TextServlet("download app"));

        String response = sendRawRequest(
                "GET /app/download/user?id=123 HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n"
        );

        assertTrue(response.contains("download app"));
        assertFalse(response.contains("general app"));
    }

    @Test
    public void testFallbackToShorterPrefix() throws Exception {
        startServer(2);

        server.addServlet("GET", "/app", new TextServlet("app fallback"));

        String response = sendRawRequest(
                "GET /app/index.html HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n"
        );

        assertTrue(response.contains("app fallback"));
    }

    @Test
    public void testRootServletAsFallback() throws Exception {
        startServer(2);

        server.addServlet("GET", "/", new TextServlet("root servlet"));

        String response = sendRawRequest(
                "GET /some/unknown/path HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n"
        );

        assertTrue(response.contains("root servlet"));
    }

    @Test
    public void testPostRequestWithContent() throws Exception {
        startServer(2);

        server.addServlet("POST", "/upload", new Servlet() {
            @Override
            public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
                String body = new String(ri.getContent(), StandardCharsets.UTF_8);
                String response = "received: " + body;

                toClient.write(response.getBytes(StandardCharsets.UTF_8));
                toClient.flush();
            }

            @Override
            public void close() throws IOException {
            }
        });

        String response = sendRawRequest(
                "POST /upload HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Content-Length: 5\r\n" +
                "\r\n" +
                "hello"
        );

        assertTrue(response.contains("received: hello"));
    }

    @Test
    public void testPostRequestWithContentAfterExtraEmptyLine() throws Exception {
        startServer(2);

        server.addServlet("POST", "/upload", new Servlet() {
            @Override
            public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
                String body = new String(ri.getContent(), StandardCharsets.UTF_8);
                String response = "received: " + body;

                toClient.write(response.getBytes(StandardCharsets.UTF_8));
                toClient.flush();
            }

            @Override
            public void close() throws IOException {
            }
        });

        String response = sendRawRequest(
                "POST /upload HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Content-Length: 5\r\n" +
                "\r\n" +
                "\r\n" +
                "hello"
        );

        assertTrue(response.contains("received: hello"));
    }

    @Test
    public void testDeleteRequest() throws Exception {
        startServer(2);

        server.addServlet("DELETE", "/resource", new TextServlet("deleted"));

        String response = sendRawRequest(
                "DELETE /resource HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n"
        );

        assertTrue(response.contains("deleted"));
    }

    @Test
    public void testSameUriDifferentHttpMethodsUseDifferentServlets() throws Exception {
        startServer(2);

        server.addServlet("GET", "/item", new TextServlet("get item"));
        server.addServlet("POST", "/item", new TextServlet("post item"));
        server.addServlet("DELETE", "/item", new TextServlet("delete item"));

        String getResponse = sendRawRequest(
                "GET /item HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n"
        );

        String postResponse = sendRawRequest(
                "POST /item HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "Content-Length: 0\r\n" +
                "\r\n"
        );

        String deleteResponse = sendRawRequest(
                "DELETE /item HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n"
        );

        assertTrue(getResponse.contains("get item"));
        assertTrue(postResponse.contains("post item"));
        assertTrue(deleteResponse.contains("delete item"));
    }

    @Test
    public void testRemoveServletReturns404AfterRemoval() throws Exception {
        startServer(2);

        server.addServlet("GET", "/temp", new TextServlet("temporary"));
        server.removeServlet("GET", "/temp");

        String response = sendRawRequest(
                "GET /temp HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n"
        );

        assertTrue(response.contains("404"));
    }

    @Test
    public void testUnknownPathReturns404() throws Exception {
        startServer(2);

        String response = sendRawRequest(
                "GET /does/not/exist HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n"
        );

        assertTrue(response.contains("404"));
    }

    @Test
    public void testServerCanHandleMultipleSequentialRequests() throws Exception {
        startServer(2);

        final AtomicInteger counter = new AtomicInteger(0);

        server.addServlet("GET", "/count", new Servlet() {
            @Override
            public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
                int value = counter.incrementAndGet();
                String response = "count=" + value;

                toClient.write(response.getBytes(StandardCharsets.UTF_8));
                toClient.flush();
            }

            @Override
            public void close() throws IOException {
            }
        });

        String response1 = sendRawRequest(
                "GET /count HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n"
        );

        String response2 = sendRawRequest(
                "GET /count HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n"
        );

        assertTrue(response1.contains("count=1"));
        assertTrue(response2.contains("count=2"));
    }

    @Test
    public void testAddServletWithUriWithoutLeadingSlashStillWorks() throws Exception {
        startServer(2);

        server.addServlet("GET", "noSlash", new TextServlet("works"));

        String response = sendRawRequest(
                "GET /noSlash HTTP/1.1\r\n" +
                "Host: localhost\r\n" +
                "\r\n"
        );

        assertTrue(response.contains("works"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddServletWithNullUriThrowsException() throws Exception {
        startServer(2);

        server.addServlet("GET", null, new TextServlet("bad"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddServletWithNullServletThrowsException() throws Exception {
        startServer(2);

        server.addServlet("GET", "/bad", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveServletWithNullUriThrowsException() throws Exception {
        startServer(2);

        server.removeServlet("GET", null);
    }
}