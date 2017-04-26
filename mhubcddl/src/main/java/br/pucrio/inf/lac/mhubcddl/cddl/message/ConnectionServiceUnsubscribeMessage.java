package br.pucrio.inf.lac.mhubcddl.cddl.message;

import br.pucrio.inf.lac.mhubcddl.cddl.listeners.IConnectionListener;

/**
 * Created by lcmuniz on 16/02/17.
 */

public class ConnectionServiceUnsubscribeMessage {

    public String topic;
    public IConnectionListener listener;

    public ConnectionServiceUnsubscribeMessage(String topic, IConnectionListener listener) {
        this.topic = topic;
        this.listener = listener;
    }

}