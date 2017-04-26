package br.pucrio.inf.lac.mhubcddl.cddl.pubsub;

import br.pucrio.inf.lac.mhubcddl.cddl.listeners.IClientQoSListener;
import br.pucrio.inf.lac.mhubcddl.cddl.listeners.IPublisherListener;
import br.pucrio.inf.lac.mhubcddl.cddl.message.QueryResponseMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.message.SensorDataMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.ontology.QueryDestiny;
import br.pucrio.inf.lac.mhubcddl.cddl.ontology.QueryType;

/**
 * Created by lcmuniz on 19/02/17.
 */
public interface Publisher extends Client {

    void publish(SensorDataMessage sensorDataMessage);

    void publish(QueryResponseMessage queryResponseMessage);

    long query(QueryDestiny queryDestiny, QueryType queryType, String query);

    void cancelQuery(long return_code);

    void setCDDLFilter(String eplFilter);

    void clearCDDLFilter();

    void setPublisherListener(IPublisherListener publisherListener);

    void setPublisherQoCListener(IClientQoSListener publisherQoCListener);

}
