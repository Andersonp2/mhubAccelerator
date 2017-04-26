package br.pucrio.inf.lac.mhubcddl.cddl.listeners;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import br.pucrio.inf.lac.mhubcddl.cddl.message.Message;

public interface IConnectionListener {

    String getClientId();

    public void onMessageArrived(Message message);


}
