package br.pucrio.inf.lac.mhubcddl.cddl.pubsub;

import br.pucrio.inf.lac.mhubcddl.cddl.message.Message;

/**
 * Created by lcmuniz on 19/02/17.
 */
public interface Filter {

    void process(Message message);

    boolean isSet();

    void set(String eplFilter);
}
