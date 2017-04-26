package br.pucrio.inf.lac.mhubcddl.cddl.message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EventQueryResponseMessage extends Message implements Serializable {

    private static final long serialVersionUID = -5433094800290949481L;

    private String subscriberID;

    private EventQueryMessage eventQueryMessage;
    private List<ServiceInformationMessage> serviceInformatioMessageList = new ArrayList<ServiceInformationMessage>();

    public EventQueryResponseMessage(EventQueryMessage eqm) {
        this.eventQueryMessage = eqm;
    }

    public List<ServiceInformationMessage> getServiceInformationMessageList() {
        return serviceInformatioMessageList;
    }

    public EventQueryMessage getEventQueryMessage() {
        return eventQueryMessage;
    }


    public String getSubscriberID() {
        return subscriberID;
    }

    public void setSubscriberID(String subscriberID) {
        this.subscriberID = subscriberID;
    }
}
