package br.pucrio.inf.lac.mhubcddl.cddl.message;

/**
 * Created by lcmuniz on 16/02/17.
 */

public class ConnectionServicePublishMessage {

    public Message message;
    public String topic;
    public int reliability;
    public boolean retained;

    public ConnectionServicePublishMessage(Message message, String topic, int reliability, boolean retained) {
        this.message = message;
        this.topic = topic;
        this.reliability = reliability;
        this.retained = retained;
    }

}
