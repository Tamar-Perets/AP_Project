package agentsForTests;

import graph.Agent;
import graph.Message;

//Helper agent to catch results
public class ResultCatcher implements Agent {
    double lastResult = Double.NaN;
    
    @Override 
    public void callback(String topic, Message msg) {
        this.lastResult = msg.asDouble; 
    }
    
    // Getters
    public double getLastResult() {
    	return this.lastResult;
    }
    
    @Override 
    public void reset() { 
    	this.lastResult = Double.NaN; 
    }
    
    @Override 
    public String getName() { 
    	return "Catcher"; 
    }
    
    @Override 
    public void close() {}
}