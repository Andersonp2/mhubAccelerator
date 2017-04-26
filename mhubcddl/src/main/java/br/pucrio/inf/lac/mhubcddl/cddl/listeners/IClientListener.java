package br.pucrio.inf.lac.mhubcddl.cddl.listeners;

import br.pucrio.inf.lac.mhubcddl.cddl.message.Message;

/**
 * Created by bertodetacio on 09/02/17.
 */

public interface IClientListener {

    public void onConnect();

    public void onDisconnect();

}
