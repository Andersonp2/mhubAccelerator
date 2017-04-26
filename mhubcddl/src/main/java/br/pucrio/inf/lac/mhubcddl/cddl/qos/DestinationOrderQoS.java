package br.pucrio.inf.lac.mhubcddl.cddl.qos;

import java.util.Comparator;

import br.pucrio.inf.lac.mhubcddl.cddl.message.Message;

public class DestinationOrderQoS extends AbstractQoS implements
        Comparator<Message> {

    public static final int PUBLISHER_TIMESTAMP = 0;

    public static final int SUBSCRIBER_TIMESTAMP = 1;

    public static final int DEFAULT_KIND = SUBSCRIBER_TIMESTAMP;

    private int kind = DEFAULT_KIND;

    public int getKind() {
        return kind;
    }

    public void setKind(int kind) throws IllegalArgumentException {

        if (kind > SUBSCRIBER_TIMESTAMP || kind < PUBLISHER_TIMESTAMP) {
            throw new IllegalArgumentException(
                    "O valor não pode ser maior que " + Long.MAX_VALUE
                            + " e nem menor que " + PUBLISHER_TIMESTAMP);
        }

        this.kind = kind;
    }

    public void restoreDefaultQoS() {
        this.kind = DEFAULT_KIND;
    }

    @Override
    public int compare(Message lhs, Message rhs) {

        if (kind == SUBSCRIBER_TIMESTAMP && lhs.getReceptionTimestamp() != 0
                && rhs.getReceptionTimestamp() != 0) {

            if (lhs.getReceptionTimestamp() > rhs.getReceptionTimestamp()) {
                return -1;
            }
            if (lhs.getReceptionTimestamp() < rhs.getReceptionTimestamp()) {
                return 1;
            }

            return 0;

        } else if (kind == PUBLISHER_TIMESTAMP
                && lhs.getMeasurementTime() != 0
                && rhs.getMeasurementTime() != 0) {

            if (lhs.getMeasurementTime() > rhs.getMeasurementTime()) {
                return -1;
            }
            if (lhs.getMeasurementTime() < rhs.getMeasurementTime()) {
                return 1;
            }

            return 0;

        }

        return 0;
    }

}
