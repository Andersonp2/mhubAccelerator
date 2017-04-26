package br.pucrio.inf.lac.mhubcddl.cddl.message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A service response message that the directory sends to the subscriber
 * containing responses to queries about publishers.
 * The message is composed by the service query message that was sended
 * by the subscriber and a list of service information messages that satisfies
 * the query. See ServiceQueryMessage and ServiceInformationMessage for details
 * about these messages.
 *
 * @author lcmuniz
 * @since June 26, 2016
 */
public class QueryResponseMessage extends Message implements Serializable {

    private static final long serialVersionUID = -5433094800290949481L;

    private String subscriberID;

    private QueryMessage queryMessage;
    private List<ServiceInformationMessage> serviceInformatioMessageList = new ArrayList<ServiceInformationMessage>();

    public QueryResponseMessage(QueryMessage qm) {
        this.queryMessage = qm;
    }

    public List<ServiceInformationMessage> getServiceInformationMessageList() {
        return serviceInformatioMessageList;
    }

    public QueryMessage getQueryMessage() {
        return queryMessage;
    }

    public String getSubscriberID() {
        return subscriberID;
    }

    public void setSubscriberID(String subscriberID) {
        this.subscriberID = subscriberID;
    }
}
