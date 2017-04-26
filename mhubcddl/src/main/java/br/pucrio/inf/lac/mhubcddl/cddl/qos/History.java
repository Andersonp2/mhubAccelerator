package br.pucrio.inf.lac.mhubcddl.cddl.qos;


import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import br.pucrio.inf.lac.mhubcddl.cddl.message.Message;
import br.pucrio.inf.lac.mhubcddl.cddl.util.Time;

public class History {

    private Vector<Message> queueMessages = new Vector<Message>();

    private HistoryQoS historyQoS;

    private DestinationOrderQoS destinationOrderQoS;

    private Time time = Time.getInstance();

    public History(HistoryQoS historyQoS, DestinationOrderQoS destinationOrderQoS) {
        this.historyQoS = historyQoS;
        this.destinationOrderQoS = destinationOrderQoS;
    }

    public synchronized boolean insert(Message message) {

        if (!contains(message)) {

            if (historyQoS.getKind() == HistoryQoS.KEEP_ALL) {

                queueMessages.add(message);

                Collections.sort(queueMessages, destinationOrderQoS);

                return true;

            } else if (historyQoS.getKind() == HistoryQoS.KEEP_LAST && historyQoS.getDepth() > 0) {

                this.queueMessages.add(message);

                Collections.sort(queueMessages, destinationOrderQoS);

                if (queueMessages.size() > 0 && queueMessages.size() > historyQoS.getDepth()) {
                    queueMessages.remove(queueMessages.size() - 1);

                    return true;
                }
            }
            else{
                return false;
            }

        }
        return false;
    }


    public synchronized Message read() {
        Message sample = null;
        if (queueMessages.size() > 0) {
            sample = queueMessages.get(0);
        }
        return sample;
    }

    public synchronized Message read(int index) {
        Message sample = null;
        if (queueMessages.size() > 0) {
            sample = queueMessages.get(index);
        }
        return sample;
    }


    public synchronized Message take() {
        Message sample = null;
        if (queueMessages.size() > 0) {
            sample = queueMessages.remove(0);
        }
        return sample;
    }

    public synchronized Message take(int index) {
        Message sample = null;
        if (queueMessages.size() > index) {
            sample = queueMessages.remove(index);
        }
        return sample;
    }


    public synchronized List<? extends Message> readAll() {
        List<Message> samples = new ArrayList<Message>(queueMessages);
        return samples;
    }

    public synchronized List<? extends Message> takeAll() {
        List<Message> samples = new ArrayList<Message>(queueMessages);
        queueMessages.clear();
        return samples;
    }

    public synchronized void remove(Message message) {
        if (queueMessages.contains(message)) {
            queueMessages.remove(message);
        }
    }

    public synchronized void remove(int index) {
        if (queueMessages.size() > index) {
            queueMessages.remove(index);
        }
    }

    public boolean contains(Message message) {
        return queueMessages.contains(message);
    }

    public boolean isEmpty() {
        return queueMessages.isEmpty();
    }

    public boolean isFull() {
        if (historyQoS.getKind() == HistoryQoS.KEEP_ALL) {
            return false;
        } else {
            if (queueMessages.size() == historyQoS.getDepth()) {
                return true;
            } else {
                return false;
            }
        }
    }

    public synchronized void clear() {
        queueMessages.clear();
    }

    public int size() {
        return queueMessages.size();
    }

    public DestinationOrderQoS getDestinationOrderQoS() {
        return destinationOrderQoS;
    }

    public void setDestinationOrderQoS(DestinationOrderQoS destinationOrderQoS) {
        this.destinationOrderQoS = destinationOrderQoS;
    }

    public HistoryQoS getHistoryQoS() {
        return historyQoS;
    }

    public void setHistoryQoS(HistoryQoS historyQoS) {
        this.historyQoS = historyQoS;
    }


}
