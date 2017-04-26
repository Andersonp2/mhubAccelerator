package br.pucrio.inf.lac.mhubcddl.cddl.pubsub;

import br.pucrio.inf.lac.mhubcddl.cddl.message.Message;
import br.pucrio.inf.lac.mhubcddl.cddl.qos.LivelinessQoS;
import br.pucrio.inf.lac.mhubcddl.cddl.qos.ReliabilityQoS;

/**
 * Created by lcmuniz on 05/03/17.
 */
public interface Client {

    void setReliabilityQoS(ReliabilityQoS reliabilityQoS);

    void setLivelinessQoS(LivelinessQoS livelinessQoS);

    void send(Message message, String topic);
}
