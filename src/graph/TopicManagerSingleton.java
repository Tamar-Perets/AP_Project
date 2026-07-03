package graph; 

import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;
import java.util.ArrayList;

public class TopicManagerSingleton {
	
	// static inner class
	public static class TopicManager{
		private static final TopicManager instance = new TopicManager();
		private ConcurrentHashMap<String, Topic> map;
		
		// private constructor (only TopicManager and TopicManagerSingelton can call it)
		private TopicManager() {
			map = new ConcurrentHashMap<>();
		}
		
		// this method get topic name, return it if there is a topic map to this name in the table,
		//		or if not- create it, add to the table and return it.
		public Topic getTopic(String topicName) {
			return map.computeIfAbsent(topicName, name -> new Topic(name));
		}
		
		// this method return collection of all topics in the map (copy of the Topics in map)
		public Collection<Topic> getTopics() {
			return new ArrayList<Topic>(map.values());
		}
		
		// this method clear all Topics from map
		public void clear() {
			map.clear();
		}
	}
	
	// at first call- instance is create, at the next call- return the value of instance (pointer to the data it save)
	public static TopicManager get(){
		return TopicManager.instance;
		}
}
