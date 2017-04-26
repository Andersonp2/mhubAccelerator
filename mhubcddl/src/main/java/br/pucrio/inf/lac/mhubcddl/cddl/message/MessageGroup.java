package br.pucrio.inf.lac.mhubcddl.cddl.message;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class MessageGroup extends Message {

    private boolean isHistoricalMessage = false;

    private Vector<Message> messagesQueue = new Vector<Message>();

    public MessageGroup() {
        // TODO Auto-generated constructor stub
    }

    public MessageGroup(List<? extends Message> messages) {
        messagesQueue.addAll(messages);
    }

    public void add(Message message) {
        messagesQueue.add(message);
    }

    public void remove(Message message) {
        messagesQueue.remove(message);
    }

    public Message getFirst() {
        Message message = null;
        if (messagesQueue.size() > 0) {
            message = messagesQueue.get(0);
        }
        return message;
    }

    public Message takeFirst() {
        Message message = null;
        if (messagesQueue.size() > 0) {
            message = messagesQueue.remove(0);
        }
        return message;
    }

    public Message getLast() {
        Message sample = null;
        if (messagesQueue.size() > 0) {
            sample = messagesQueue.get(messagesQueue.size() - 1);
        }
        return sample;
    }

    public Message takeLast() {
        Message message = null;
        if (messagesQueue.size() > 0) {
            message = messagesQueue.remove(messagesQueue.size() - 1);
        }
        return message;
    }

    public ArrayList<Message> getAll() {
        ArrayList<Message> messages = new ArrayList<Message>(messagesQueue);
        return messages;
    }

    public ArrayList<Message> takeAll() {
        ArrayList<Message> messages = new ArrayList<Message>(messagesQueue);
        messagesQueue.clear();
        return messages;
    }

    public boolean isHistoricalMessage() {
        return isHistoricalMessage;
    }

    public void setHistoricalMessage(boolean isHistoricalMessage) {
        this.isHistoricalMessage = isHistoricalMessage;
    }

    public int getSize() {
        return messagesQueue.size();
    }

    public boolean isEmpty() {
        return messagesQueue.isEmpty();
    }

}
