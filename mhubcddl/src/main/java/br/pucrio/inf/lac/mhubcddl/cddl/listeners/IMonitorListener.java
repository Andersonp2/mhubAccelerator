package br.pucrio.inf.lac.mhubcddl.cddl.listeners;

import br.pucrio.inf.lac.mhubcddl.cddl.message.Message;

/**
 * Created by lcmuniz on 19/02/17.
 */
public interface IMonitorListener {
    public void onEvent(Message cim);
}
