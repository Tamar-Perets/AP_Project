package server; // package server

import java.io.BufferedReader;
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
        if (firstLine == null) return null;
        
        // ** extract method
        // split to method and uri
        String[] parts = firstLine.trim().split("\\s+");
        // TODO if (parts.length < 2) throw new IOException("Invalid HTTP request format");
        String command = parts[0];	// first word is the method
        // Basic validation: Check if command is GET, POST, or DELETE
        if (!command.equals("GET") && !command.equals("POST") && !command.equals("DELETE")) {
            // TODO throw new IOException("Method not allowed: " + command);
        	return null;
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

	     // read the body: optional extra parameters section, then content section
	     byte[] content = new byte[0];
	
	     if (contentLength > 0) {
	         StringBuilder rawBody = new StringBuilder();
	
	         // Read only characters that are already available.
	         // This avoids blocking on Socket when body has no trailing newline.
	         while (reader.ready()) {
	             int ch = reader.read();
	             if (ch == -1) {
	                 break;
	             }
	             rawBody.append((char) ch);
	         }
	
	         String normalizedBody = rawBody.toString().replace("\r\n", "\n");
	
	         StringBuilder bodyText = new StringBuilder();
	         boolean pastSeparator = false;
	
	         String[] lines = normalizedBody.split("\n", -1);
	
	         for (String curLine : lines) {
	             if (curLine.isEmpty()) {
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
	                     // No metadata section exists.
	                     // This line is probably actual content immediately after headers.
	                     pastSeparator = true;
	                     bodyText.append(curLine).append('\n');
	                 }
	             } else {
	                 bodyText.append(curLine).append('\n');
	             }
	         }
	
	         content = bodyText.toString().getBytes(StandardCharsets.UTF_8);
	     }
                
        
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
}
