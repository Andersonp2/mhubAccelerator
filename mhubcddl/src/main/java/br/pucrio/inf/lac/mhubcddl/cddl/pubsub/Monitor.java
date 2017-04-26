package br.pucrio.inf.lac.mhubcddl.cddl.pubsub;

import br.pucrio.inf.lac.mhubcddl.cddl.listeners.IMonitorListener;
import br.pucrio.inf.lac.mhubcddl.cddl.message.Message;

/**
 * Created by lcmuniz on 19/02/17.
 */
public interface Monitor {

    int getNumQueries();

    void messageArrived(Message message);

    long addQuery(String query, final IMonitorListener monitorListener);
}
