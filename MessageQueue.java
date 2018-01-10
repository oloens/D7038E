package mygame;
import java.util.ArrayList;
import mygame.Util.MyAbstractMessage;

/**
 * A MessageQueue stores an arraylist of AbsractMessages (packets in our implementation).
 * Synchronized enqueues and removes.
 *
 * @author Anton Eliasson, Jonathan Olsson, Olof Enström
 */
public class MessageQueue {
    private ArrayList<MyAbstractMessage> queue;
    public MessageQueue() {
        queue = new ArrayList<MyAbstractMessage>();
    }
    public synchronized void enqueue(Util.MyAbstractMessage m) {
        this.queue.add(m);
    }
    public synchronized boolean isEmpty() {
        return this.queue.isEmpty();
    }
    public synchronized Util.MyAbstractMessage pop() {
        try {
            return this.queue.remove(0);
        }
        catch (Exception e) {
            return null;
        }
    }
}