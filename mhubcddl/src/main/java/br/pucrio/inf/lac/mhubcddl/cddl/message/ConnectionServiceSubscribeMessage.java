package br.pucrio.inf.lac.mhubcddl.cddl.message;

import br.pucrio.inf.lac.mhubcddl.cddl.listeners.IConnectionListener;

/**
 * Created by lcmuniz on 16/02/17.
 */

public class ConnectionServiceSubscribeMessage {

    public String topic;
    public int reliability;
    public IConnectionListener listener;

    public ConnectionServiceSubscribeMessage(String topic, int reliability, IConnectionListener listener) {
        this.topic = topic;
        this.reliability = reliability;
        this.listener = listener;
    }

}