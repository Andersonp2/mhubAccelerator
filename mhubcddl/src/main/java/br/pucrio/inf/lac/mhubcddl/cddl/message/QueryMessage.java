package br.pucrio.inf.lac.mhubcddl.cddl.message;

import java.io.Serializable;

import br.pucrio.inf.lac.mhubcddl.cddl.ontology.QueryType;

/**
 * A service query message that the subscriber sends to the directory asking for
 * informations about publishers. The message is composed by the subscriber id
 * and a query. The query is created on a specific language called Information
 * Context Query Language (ICCL). A query example: service.location with
 * qoc.precision = 98 The example above, asks the directory for all publisher
 * that can publish location context information with a precision of 98%. See
 * the iccl documentation for more information.
 *
 * @author lcmuniz
 * @since June 26, 2016
 */
public class QueryMessage extends Message implements Serializable {

    private static final long serialVersionUID = 7658311196412694942L;

    private String timestamp; // message created time
    private String query;
    private QueryType type;
    private long returnCode;

    public QueryMessage(String publisherId, String query, QueryType type, long returnCode) {
        this.timestamp = new Long(System.currentTimeMillis()).toString();
        this.setPublisherID(publisherId);
        this.query = query;
        this.type = type;
        this.returnCode = returnCode;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getQuery() {
        return query;
    }

    public QueryType getType() {
        return type;
    }

    public long getReturnCode() {
        return returnCode;
    }

}
