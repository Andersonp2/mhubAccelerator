package br.pucrio.inf.lac.mhubcddl.cddl.message;

import com.android.internal.util.Predicate;

import java.io.Serializable;

public class EventQueryMessage extends Message implements Serializable {

    private static final long serialVersionUID = 7658311196412694942L;
    private final long returnCode;
    private Predicate<SensorDataMessage> predicate;
    private String sql;
    private String timestamp; // message created time

    public EventQueryMessage(String publisherId, String sql, long returnCode) {
        this.timestamp = new Long(System.currentTimeMillis()).toString();
        this.setPublisherID(publisherId);
        this.sql = sql;
        this.returnCode = returnCode;
    }

    public EventQueryMessage(String publisherId, Predicate<SensorDataMessage> predicate, long returnCode) {
        this.timestamp = new Long(System.currentTimeMillis()).toString();
        this.setPublisherID(publisherId);
        this.predicate = predicate;
        this.returnCode = returnCode;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getSQL() {
        return sql;
    }

    public Predicate<SensorDataMessage> getPredicate() {
        return predicate;
    }

    public long getReturnCode() {
        return returnCode;
    }


}
