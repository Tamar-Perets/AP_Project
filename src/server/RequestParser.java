package server; // package server

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class RequestParser {
	
    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {        
		// read the first line of the request
    	String firstLine = reader.readLine();
        if (firstLine == null || firstLine.isEmpty()) 
        	return new RequestInfo("", "", new String[0], new HashMap<>(), new byte[0]);;
        
        // ** extract method
        // split to method and uri
        String[] parts = firstLine.trim().split("\\s+");
        // TODO if (parts.length < 2) throw new IOException("Invalid HTTP request format");
        String command = parts[0];	// first word is the method
        // Basic validation: Check if command is GET, POST, or DELETE
        if (!command.equals("GET") && !command.equals("POST") && !command.equals("DELETE")) {
            // TODO throw new IOException("Method not allowed: " + command);
        	return new RequestInfo("", "", new String[0], new HashMap<>(), new byte[0]);
        }
        
        // ** extract parameters and handle uri
        String fullUri = parts[1];	// after the method, and to the end of the row- this is the uri
        int firstQuestionMark = fullUri.indexOf('?'); // location of first ?
        String path; // only the path part of the uri (without parameters)
        Map<String, String> parameters = new HashMap<>(); // Thread-Safe map for parameters
        
        if (firstQuestionMark != -1) {
            // check if there is another ? - if yes it i an error TODO - canceled
            /*if (fullUri.indexOf('?', firstQuestionMark + 1) != -1) {
                throw new IOException("Invalid URI: Multiple '?' found");
            }*/
            // else - the part from the left of the ? is the uri path
            path = fullUri.substring(0, firstQuestionMark);
            // exstract parameters (keys and values) from the part 
            // from the right side of the ?, split to parties of key and values
            String[] params = fullUri.substring(firstQuestionMark + 1).split("&");
            
            // split each couple and insert to the map
			for (String p : params) {
			    if (p.isEmpty()) {
			        continue;
			    }
			
			    String[] kv = p.split("=", -1);
			    
			    // value can be empty, key not
			    if (kv.length == 2 && !kv[0].isEmpty()) {
			        parameters.put(kv[0], kv[1]);
			    }
			}
        } else {
        	// there is no ? in the path (no parameters)
            path = fullUri;
        }
        
        // ** extract segments
        // Split the path into segments (e.g., /api/resource -> ["api", "resource"])
        // We filter out empty strings that occur from the starting "/"
        String[] segments = path.split("/");
        List<String> cleanSegments = new ArrayList<>();
        for (String s : segments) if (!s.isEmpty()) cleanSegments.add(s);
        String[] uriSegments = cleanSegments.toArray(new String[0]);
        
        // ** read the contect
        // read the headers, exstract contect-lenght
        int contentLength = 0;
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
			String[] headerParts = line.split(":", 2);
			if (headerParts.length == 2 && headerParts[0].trim().equalsIgnoreCase("Content-Length")) {
			    contentLength = Integer.parseInt(headerParts[1].trim());
			}
        }    
        
        /**/
        // read the content (and form-parameters if there are)
        
     // read body: optional extra parameters section, then content section
        ByteArrayOutputStream contentBuffer = new ByteArrayOutputStream();

        boolean pastSeparator = false;

        while (reader.ready()) {
            String curLine = reader.readLine();

            if (curLine == null) {
                break;
            }

            if (curLine.isEmpty()) {
                if (pastSeparator) {
                    // empty line after content started -> end of content
                    break;
                }

                // first empty line after extra parameters section
                pastSeparator = true;
                continue;
            }

            if (!pastSeparator) {
                int eqIdx = curLine.indexOf('=');

                if (eqIdx >= 0) {
                    String key = curLine.substring(0, eqIdx);
                    String value = curLine.substring(eqIdx + 1);

                    if (!key.isEmpty()) {
                        parameters.put(key, value);
                    }
                } else {
                    // no extra-parameters section exists,
                    // so this line is probably the beginning of the content
                    pastSeparator = true;
                    contentBuffer.write(curLine.getBytes(StandardCharsets.UTF_8));
                    contentBuffer.write('\n');
                }
            } else {
                contentBuffer.write(curLine.getBytes(StandardCharsets.UTF_8));
                contentBuffer.write('\n');
            }
        }

        byte[] content = contentBuffer.toByteArray();
        
	    
        // create and return RequestInfo object that has all neccessary data
        return new RequestInfo(command, fullUri, uriSegments, parameters, content);
    }
	
	// RequestInfo given internal class
    public static class RequestInfo {
        private final String httpCommand;
        private final String uri;
        private final String[] uriSegments;
        private final Map<String, String> parameters;
        private final byte[] content;

        public RequestInfo(String httpCommand, String uri, String[] uriSegments, Map<String, String> parameters, byte[] content) {
            this.httpCommand = httpCommand;
            this.uri = uri;
            this.uriSegments = uriSegments;
            this.parameters = parameters;
            this.content = content;
        }
        
        // return http method (for example GET, POST, etc.)
        public String getHttpCommand() {
            return httpCommand;
        }
        
        // return full uri (for example /api/resource?id=123&name=test)
        public String getUri() {
            return uri;
        }
        
        // return the segments of the uri (for example “resource “,”api").
        public String[] getUriSegments() {
            return uriSegments;
        }
        
        // return map of parameters (keys) and their value (for example id = 123, name = test)
        public Map<String, String> getParameters() {
            return parameters;
        }
        
        // return http request content
        public byte[] getContent() {
            return content;
        }
    }
}
