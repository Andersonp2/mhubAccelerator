package br.pucrio.inf.lac.mhubcddl.cddl.listeners;

import br.pucrio.inf.lac.mhubcddl.cddl.message.Message;
import br.pucrio.inf.lac.mhubcddl.cddl.qos.History;

/**
 * Created by bertodetacio on 11/01/17.
 */

public interface IClientQoSListener {


    public void onExpectedDeadlineMissed();

    public void onExpectedDeadlineFulfilled();

    public void onExpectedLivelinessMissed();

    public void onExpectedLivelinessFulfilled();

    public void onLifespanExpired(Message message);

    public void onClientConnectionChangedStatus(String clientId, int status);


}
