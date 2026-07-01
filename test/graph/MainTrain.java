package graph;

//import java.lang.reflect.Method;
//import java.lang.reflect.Modifier;
import java.util.Random;
import org.junit.Test;

import graph.TopicManagerSingleton.TopicManager;


public class MainTrain { // simple tests to get you going...


    
	// cerate abstract agent class for test
    public static abstract class AAgent implements Agent{
        public void reset() {}
        public void close() {}
        public String getName(){
            return getClass().getName(); // return the fully class name of the object
        }
    }

    // cerate agent class for test
    public static class TestAgent1 extends AAgent{

        double sum=0;  	// number of callbacks - unctil 5
        int count=0;	// sum of messages as double received
        TopicManager tm=TopicManagerSingleton.get();
        
        // this function subscribe the agent to topic named "Numbers"
        public TestAgent1(){
            tm.getTopic("Numbers").subscribe(this);
        }

        @Override
        public void callback(String topic, Message msg) {
            count++;
            sum+=msg.asDouble;
            
            // every 5 callbacks
            if(count%5==0){
            	// publish at topic "sum" a message with total sum until now
                tm.getTopic("Sum").publish(new Message(sum));
                count=0;
            }

        }
        
    }

    public static class TestAgent2 extends AAgent{

        double sum=0;
        TopicManager tm=TopicManagerSingleton.get();

        public TestAgent2(){
            tm.getTopic("Sum").subscribe(this);
        }

        @Override
        public void callback(String topic, Message msg) {
            sum=msg.asDouble;
        }

        public double getSum(){
            return sum;
        }
        
    }
    
    @Test
    public static void testAgents(){        
        TopicManager tm=TopicManagerSingleton.get();
        TestAgent1 a=new TestAgent1();
        TestAgent2 a2=new TestAgent2();        
        double sum=0;
        for(int c=0;c<3;c++){
            Topic num=tm.getTopic("Numbers");
            Random r=new Random();
            for(int i=0;i<5;i++){
                int x=r.nextInt(1000);
                num.publish(new Message(x));
                sum+=x;
            }
            double result=a2.getSum();
            if(result!=sum){
                System.out.println("your code published a wrong result (-10)");
            }
        }
        a.close();
        a2.close();
    }

        
    public static void main(String[] args) {
        testAgents();        
        System.out.println("done");
    }
}
