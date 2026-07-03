package server;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RequestParser {

    /**
     * Holds the parsed information extracted from an HTTP request.
     *
     * <p>The object contains the HTTP command, the original URI, URI path
     * segments, request parameters and optional content bytes.</p>
     */
    public static class RequestInfo {
        private final String httpCommand;
        private final String uri;
        private final String[] uriSegments;
        private final Map<String, String> parameters;
        private final byte[] content;

        /**
         * Creates a new parsed request information object.
         *
         * @param httpCommand the HTTP method, such as GET or POST
         * @param uri the original URI, including query parameters if present
         * @param uriSegments the URI path split into segments
         * @param parameters request parameters extracted from the URI or body
         * @param content raw request content bytes
         */
        public RequestInfo(String httpCommand, String uri, String[] uriSegments,
                           Map<String, String> parameters, byte[] content) {
            this.httpCommand = httpCommand;
            this.uri = uri;
            this.uriSegments = uriSegments;
            this.parameters = parameters;
            this.content = content;
        }

        public String getHttpCommand() {
            return httpCommand;
        }

        public String getUri() {
            return uri;
        }

        public String[] getUriSegments() {
            return uriSegments;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }

        public byte[] getContent() {
            return content;
        }
    }

    /**
     * Parses a single HTTP request from the given reader.
     *
     * <p>The method supports both the simplified request format used in the
     * course tests and real multipart/form-data uploads sent by a browser.</p>
     *
     * @param reader the input source containing the HTTP request
     * @return a RequestInfo object containing the parsed request data
     * @throws IOException if reading from the input source fails
     */
    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {
        String requestLine = reader.readLine();

        if (requestLine == null || requestLine.isEmpty()) {
            return new RequestInfo("", "", new String[0], new HashMap<>(), new byte[0]);
        }

        String[] firstLineParts = requestLine.split("\\s+");
        String httpCommand = firstLineParts.length > 0 ? firstLineParts[0] : "";
        String fullUri = firstLineParts.length > 1 ? firstLineParts[1] : "";

        Map<String, String> parameters = new HashMap<>();

        String uriWithoutParams = fullUri;
        int questionMarkIndex = fullUri.indexOf('?');

        if (questionMarkIndex != -1) {
            uriWithoutParams = fullUri.substring(0, questionMarkIndex);
            String queryString = fullUri.substring(questionMarkIndex + 1);
            parseParameters(queryString, parameters);
        }

        String[] uriSegments = parseUriSegments(uriWithoutParams);

        int contentLength = 0;
        String contentType = "";
        String line;

        // Read headers until an empty line.
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            String lower = line.toLowerCase();

            if (lower.startsWith("content-length:")) {
                contentLength = Integer.parseInt(line.substring(line.indexOf(':') + 1).trim());
            }

            if (lower.startsWith("content-type:")) {
                contentType = line.substring(line.indexOf(':') + 1).trim();
            }
        }

        ByteArrayOutputStream contentBuffer = new ByteArrayOutputStream();

        /*
         * Browser file uploads are sent as multipart/form-data.
         * In this case we must read exactly Content-Length characters,
         * otherwise reader.ready() may stop too early.
         */
        if (contentType.toLowerCase().contains("multipart/form-data") && contentLength > 0) {
            for (int i = 0; i < contentLength; i++) {
                int c = reader.read();

                if (c == -1) {
                    break;
                }

                contentBuffer.write(c);
            }
        } else {
            /*
             * Simplified format used by the course tests:
             * after headers, there may be additional key=value parameters,
             * then an empty line, then content.
             */
            while (reader.ready()) {
                reader.mark(1000);
                line = reader.readLine();

                if (line == null || line.isEmpty()) {
                    break;
                }

                int eqIndex = line.indexOf('=');

                if (eqIndex != -1) {
                    String key = line.substring(0, eqIndex);
                    String value = line.substring(eqIndex + 1);
                    parameters.put(key, value);
                } else {
                    reader.reset();
                    break;
                }
            }

            while (reader.ready()) {
                line = reader.readLine();

                if (line == null || line.isEmpty()) {
                    break;
                }

                contentBuffer.write(line.getBytes());
                contentBuffer.write('\n');
            }
        }

        return new RequestInfo(
                httpCommand,
                fullUri,
                uriSegments,
                parameters,
                contentBuffer.toByteArray()
        );
    }

    private static void parseParameters(String queryString, Map<String, String> parameters) {
        if (queryString == null || queryString.isEmpty()) {
            return;
        }

        String[] pairs = queryString.split("&");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);

            if (keyValue.length == 2) {
                parameters.put(keyValue[0], keyValue[1]);
            } else if (keyValue.length == 1 && !keyValue[0].isEmpty()) {
                parameters.put(keyValue[0], "");
            }
        }
    }

    private static String[] parseUriSegments(String uri) {
        if (uri == null || uri.equals("/") || uri.isEmpty()) {
            return new String[0];
        }

        String cleanUri = uri.startsWith("/") ? uri.substring(1) : uri;

        if (cleanUri.isEmpty()) {
            return new String[0];
        }

        return cleanUri.split("/");
    }
}
