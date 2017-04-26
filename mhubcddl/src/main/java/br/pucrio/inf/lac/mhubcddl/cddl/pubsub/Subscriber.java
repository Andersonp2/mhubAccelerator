package br.pucrio.inf.lac.mhubcddl.cddl.pubsub;

import br.pucrio.inf.lac.mhubcddl.cddl.listeners.IClientQoSListener;
import br.pucrio.inf.lac.mhubcddl.cddl.listeners.ISubscriberListener;
import br.pucrio.inf.lac.mhubcddl.cddl.listeners.IMonitorListener;
import br.pucrio.inf.lac.mhubcddl.cddl.message.ServiceInformationMessage;

public interface Subscriber extends Client {

    void setSubscriberListener(ISubscriberListener subscriberListener);

    void setSubscriberQoSListener(IClientQoSListener subscriberQoSListener);

    void setFilter(String eplFilter);

    void subscribeSensorDataTopicByPublisherId(String s);

    void subscribeSensorDataTopicByServiceInformationMessage(ServiceInformationMessage sim);

    void subscribeSensorDataTopicByPublisherIdAndServiceName(String publisher_id, String serviceName);

    void unsubscribeSensorDataTopicByPublisherIdAndServiceName(String publisher_id, String serviceName);

    void subscribeLivelenessTopic();

    void subscribeConnectionChangedStatusTopic();

    void addMonitorQuery(String query, IMonitorListener monitorListener);
}

