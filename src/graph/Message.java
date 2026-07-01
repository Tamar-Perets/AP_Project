package graph; // TODO: for project, change package name to graph

import java.util.Date;
import java.nio.charset.StandardCharsets;

public class Message {
	public final byte[] data;
	public final String asText;
	public final double asDouble;
	public final Date date;
	
	// Constructors
	public Message(String text) {
		this.data = text.getBytes(StandardCharsets.UTF_8);
		this.asText = text;
		double tempValue;
		try {
		    tempValue = Double.parseDouble(text);
		} 
		catch (NumberFormatException e) { 
		    tempValue = Double.NaN;
		}
		this.asDouble = tempValue;
		this.date = new Date();	
	}
	
	public Message(byte[] data) {
		this(new String(data, StandardCharsets.UTF_8));
	}
	
	public Message(double num) {
		this(String.valueOf(num));
	}

}
