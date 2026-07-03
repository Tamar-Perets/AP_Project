package main;

import servlets.ConfLoader;
import servlets.HtmlLoader;
import server.MyHTTPServer;
import server.HTTPServer;
import servlets.TopicDisplayer;

// this class operate the server etc.
// to access the server, access from browser to: http://localhost:8080/app/index.html
// to stop, enter anything in the console here.
public class Main {
    public static void main(String[] args) throws Exception {
        // Initialize your server (assuming MyHTTPServer is your implementation)
        HTTPServer server = new MyHTTPServer(8080, 5); 

        // Add the HtmlLoader Servlet
        server.addServlet("GET", "/app/", new HtmlLoader("html_files"));
        server.addServlet("POST", "/upload", new ConfLoader());
        server.addServlet("GET", "/publish", new TopicDisplayer());

        server.start();
        System.out.println("Server is running. Press Enter to stop.");
        System.in.read();
        server.close();
        System.out.println("done");
    }
}
