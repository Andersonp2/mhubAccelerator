package br.pucrio.inf.lac.mhubcddl.cddl.listeners;

import br.pucrio.inf.lac.mhubcddl.cddl.message.Message;

/**
 * Created by lcmuniz on 26/02/17.
 */

public interface IPublisherListener extends IClientListener {

    void onMessageDelivered(Message message);

}
