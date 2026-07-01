package server; // package server

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import server.RequestParser.RequestInfo;
import servlets.Servlet;


public class MyHTTPServer extends Thread implements HTTPServer{
	
	final int port;
	final int nThreads;
	private final Map<String, Servlet> getMap;
	private final Map<String, Servlet> postMap;
	private final Map<String, Servlet> deleteMap;
	private ExecutorService threadPool;
	private ServerSocket serverSocket;
	private volatile boolean stop;
    
	// constructor
    public MyHTTPServer(int port,int nThreads){
    	this.port = port;
    	this.nThreads = nThreads;
    	
    	this.getMap = new ConcurrentHashMap<>();
    	this.postMap = new ConcurrentHashMap<>();
    	this.deleteMap = new ConcurrentHashMap<>();
    	
    	this.threadPool = null;
    	this.serverSocket = null;
    	this.stop = false;
    }
    
    // start
    @Override
    public synchronized void start() {
    	if (this.serverSocket != null) return;
    	
		if (this.serverSocket != null) {
		        return; // already started
		    }
		
		    this.stop = false;
		
		    try {
		        this.threadPool = Executors.newFixedThreadPool(this.nThreads);
		
		        this.serverSocket = new ServerSocket();
		        this.serverSocket.setReuseAddress(true);
		        this.serverSocket.bind(new InetSocketAddress(this.port));
		        this.serverSocket.setSoTimeout(1000);
		
		    } catch (IOException e) {
		    	System.err.println("Failed to start server on port " + port + ": " + e.getMessage());
		    	return;
		    }
		
		    super.start();
    }
    
    // this method add servlet to the corresponding map
    @Override
    public void addServlet(String httpCommand, String uri, Servlet s){
    	// input validation
    	if (httpCommand == null || uri == null || s == null) {
            // TODO throw new IllegalArgumentException("URI and Servlet cannot be null");
    		return;
        }
        
        // normalize uri (to start with /) TODO
    	//String cleanUri = normalizeUri(uri);
    	
    	switch (httpCommand.toUpperCase()) {
        case "GET":
            this.getMap.put(uri, s);
            break;
        case "POST":
            this.postMap.put(uri, s);
            break;
        case "DELETE":
            this.deleteMap.put(uri, s);
            break;
        default:
            // TODO System.out.println("Method not supported (should be GET / POST / DELETE)");
        	return;
    	}
    }
    
    // this method remove servlet from the corresponding map
    @Override
    public void removeServlet(String httpCommand, String uri){
    	// input validation
    	if (httpCommand == null || uri == null) {
            // TODO throw new IllegalArgumentException("URI and Servlet cannot be null");
    		return;
        }
        
        // normalize uri (to start with /)
    	//String cleanUri = normalizeUri(uri);
    	
    	switch (httpCommand.toUpperCase()) {
        case "GET":
            this.getMap.remove(uri);
            break;
        case "POST":
            this.postMap.remove(uri);
            break;
        case "DELETE":
            this.deleteMap.remove(uri);
            break;
        default:
            // TODO System.out.println("Method not supported (should be GET / POST / DELETE)");
        	return;
    	}
    }
    
    
    @Override
    public void run(){
    	
    	System.out.println("Server is listening on port " + this.port);
    	
    	while(!stop) {
    		// wait for request from the client
    		Socket clientSocket;
    		try {
    			clientSocket = this.serverSocket.accept();
    			System.out.println("Client connected: " + clientSocket.getRemoteSocketAddress());

			} catch (java.net.SocketTimeoutException e) {
			    continue;
			} catch (IOException e) {
			    if (!stop) {
			        System.out.println("Server socket is closed or failed to accept: " + e.getMessage());
			    }
			    continue;
			}

    		
    		// when request received:
    		
    		// open new thread to handle the query
    		this.threadPool.submit(() -> {
    		    RequestInfo ri = null;
    		    try {
    		        // define timeour of waiting for client message
    		        //clientSocket.setSoTimeout(5000); // TODO
    		        
    		        // analyze the client request
					ri = RequestParser.parseRequest(
					    new BufferedReader(
					        new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8)
					    )
					);

    		        
    		        if (ri != null) {    		        	
    		        	// take the corresponding map
    		        	Map<String, Servlet> map;
    		        	switch(ri.getHttpCommand()) {
    		        	case "GET":
    		        		map = this.getMap;
    		        		break;
    		        	case "POST":
    		        		map = this.postMap;
    		        		break;
    		        	case "DELETE":
    		        		map = this.deleteMap;
    		        		break;
    		        	default:
    		                System.out.println("Method not supported (should be GET / POST / DELETE)");
    		                return;
    		        	}
    		        	
    		        	// find the corresponding servlet (longest match)
						String requestUri = ri.getUri();
						Servlet s = null;
						String longestMatch = null;
						
						for (String prefix : map.keySet()) {
						    if (requestUri.startsWith(prefix)) {
						        if (longestMatch == null || prefix.length() > longestMatch.length()) {
						            longestMatch = prefix;
						        }
						    }
						}
						
						if (longestMatch != null) {
						    s = map.get(longestMatch);
						}

    		        	
    		        	if (s == null) {
    		        	    OutputStream out = clientSocket.getOutputStream();
    		        	    out.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes(StandardCharsets.UTF_8));
    		        	    out.flush();
    		        	}
    		        	else {
    		        		{
    		        		    OutputStream out = clientSocket.getOutputStream();
    		        		    out.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes(StandardCharsets.UTF_8));
    		        		    out.flush();
    		        		}
    		        	}	
    		        } 
    		    } catch (IOException e) {
    		    	// something went wrong with the client (maybe read from its socket)
    		        System.out.println("Error reading/parsing request from client: " + e.getMessage());
    		    } finally {
    		    	// after handling the client \ some error
    		        // disconnect the client (close the client socket)
    		        try {
    		            clientSocket.close();
    		        } catch (IOException e) {
    		            e.printStackTrace();
    		        }
    		    }
    		});
    		

    		
    	}	// end of while
    }
    
    // this method close the server, including close all running threads etc.
    @Override
    public void close(){
    	// update stop (so the server running run() will stop)
    	this.stop = true; 
        
    	// close server socket (if the server is at accept() this will throw it out)
        try {
            if (this.serverSocket != null && !this.serverSocket.isClosed()) {
                this.serverSocket.close(); 
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
        
        // close all threads at the threads-pool
		if (this.threadPool != null) {
		    this.threadPool.shutdown();
		    try {
		        if (!this.threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
		            this.threadPool.shutdownNow();
		        }
		    } catch (InterruptedException e) {
		        this.threadPool.shutdownNow();
		        Thread.currentThread().interrupt();
		    }
		}

        
        // close servlets
		Set<Servlet> servlets = ConcurrentHashMap.newKeySet();
		servlets.addAll(getMap.values());
		servlets.addAll(postMap.values());
		servlets.addAll(deleteMap.values());
		
		for (Servlet s : servlets) {
		    try {
		        s.close();
		    } catch (IOException e) {
		        System.err.println("Error closing servlet: " + e.getMessage());
		    }
		}

    }
    
   


}
