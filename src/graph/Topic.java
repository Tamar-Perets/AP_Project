package graph;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Topic {
    public final String name;
    List<Agent> subs;
    List<Agent> pubs;

    private Message lastMessage;

    public Topic(String name) {
        this.name = name;
        this.subs = new CopyOnWriteArrayList<>();
        this.pubs = new CopyOnWriteArrayList<>();
        this.lastMessage = null;
    }

    public List<Agent> getSubs() {
        return Collections.unmodifiableList(this.subs);
    }

    public List<Agent> getPubs() {
        return Collections.unmodifiableList(this.pubs);
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void subscribe(Agent a) {
        if (!this.subs.contains(a)) {
            this.subs.add(a);
        }
    }

    public void unsubscribe(Agent a) {
        this.subs.remove(a);
    }

    public void publish(Message msg) {
        this.lastMessage = msg;

        for (Agent sub : this.subs) {
            sub.callback(this.name, msg);
        }
    }

    public void addPublisher(Agent a) {
        if (!this.pubs.contains(a)) {
            this.pubs.add(a);
        }
    }

    public void removePublisher(Agent a) {
        this.pubs.remove(a);
    }
}
