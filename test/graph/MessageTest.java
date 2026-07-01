package graph;

import org.junit.Test;


public class MessageTest {
	
	@Test
	public static void testConstructors() {

        // Test String constructor
        String testString = "Hello";
        Message msgFromString = new Message(testString);
        if (!testString.equals(msgFromString.asText)) {
            System.out.println("Error: String constructor - asText does not match input string (-5)");
        }
        if (!java.util.Arrays.equals(testString.getBytes(), msgFromString.data)) {
            System.out.println("Error: String constructor - data does not match input string bytes (-5)");
        }
        if (!Double.isNaN(msgFromString.asDouble)) {
            System.out.println("Error: String constructor - asDouble should be NaN for non-numeric string (-5)");
        }
        if (msgFromString.date == null) {
            System.out.println("Error: String constructor - date should not be null (-5)");
        }
        
        // Test Bytes constructor
        testString = "57";
        byte[] testByte = testString.getBytes();
        Message msgFromBytes = new Message(testByte);
        if (!testString.equals(msgFromBytes.asText)) {
            System.out.println("Error: Bytes constructor - asText does not match input string (-5)");
        }
        if (!java.util.Arrays.equals(testByte, msgFromBytes.data)) {
            System.out.println("Error: Bytes constructor - data does not match input string bytes (-5)");
        }
        if (msgFromBytes.asDouble != 57.0) {
            System.out.println("Error: Bytes constructor - asDouble should be NaN for non-numeric string (-5)");
        }
        if (msgFromBytes.date == null) {
            System.out.println("Error: Bytes constructor - date should not be null (-5)");
        }
        
     // Test Double constructor
        double testDouble = 39.4;
        Message msgFromDouble = new Message(testDouble);
        if (!String.valueOf(testDouble).equals(msgFromDouble.asText)) {
            System.out.println("Error: Double constructor - asText does not match input string (-5)");
        }
        if (!java.util.Arrays.equals("39.4".getBytes(), msgFromDouble.data)) {
            System.out.println("Error: Double constructor - data does not match input string bytes (-5)");
        }
        if (msgFromDouble.asDouble != 39.4) {
            System.out.println("Error: Double constructor - asDouble should be NaN for non-numeric string (-5)");
        }
        if (msgFromDouble.date == null) {
            System.out.println("Error: Double constructor - date should not be null (-5)");
        }
    }

}
