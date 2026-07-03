package configs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import graph.Agent;
import graph.Topic;
import graph.TopicManagerSingleton;
import graph.TopicManagerSingleton.TopicManager;

@SuppressWarnings("serial")
public class Graph extends ArrayList<Node> {
    private TopicManager tm;

    public Graph() {
        tm = TopicManagerSingleton.get();
    }

    public boolean hasCycles() {
        for (Node node : this) {
            if (node.hasCycles()) {
                return true;
            }
        }
        return false;
    }

    public void createFromTopics() {
        this.clear();

        Collection<Topic> topics = tm.getTopics();

        HashMap<Agent, Node> agents = new HashMap<>();
        HashMap<String, Integer> agentNameCounter = new HashMap<>();

        for (Topic topic : topics) {
            Node topicNode = new Node("T" + topic.name);
            this.add(topicNode);

            ArrayList<Node> topicNeighbors = new ArrayList<>();

            for (Agent agent : topic.getSubs()) {
                Node agentNode = getOrCreateAgentNode(agent, agents, agentNameCounter);
                topicNeighbors.add(agentNode);
            }

            topicNode.setEdges(topicNeighbors);

            for (Agent agent : topic.getPubs()) {
                Node agentNode = getOrCreateAgentNode(agent, agents, agentNameCounter);
                agentNode.addEdge(topicNode);
            }
        }
    }

    private Node getOrCreateAgentNode(
            Agent agent,
            HashMap<Agent, Node> agents,
            HashMap<String, Integer> agentNameCounter) {

        if (agents.containsKey(agent)) {
            return agents.get(agent);
        }

        String baseName = "A" + agent.getName();

        int count = agentNameCounter.getOrDefault(baseName, 0) + 1;
        agentNameCounter.put(baseName, count);

        String uniqueName = count == 1 ? baseName : baseName + "_" + count;

        Node newAgentNode = new Node(uniqueName);
        this.add(newAgentNode);
        agents.put(agent, newAgentNode);

        return newAgentNode;
    }
}
