package br.pucrio.inf.lac.mhubcddl.cddl.qos;


public class DeadlineQoS extends AbstractQoS {

    public static final long DEFAULT_DEADLINE = 0;

    private long period = DEFAULT_DEADLINE;

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        if (period > Long.MAX_VALUE || period < DEFAULT_DEADLINE) {
            throw new IllegalArgumentException("O valor não pode ser menor que " + DEFAULT_DEADLINE + "e nem maior que " + Long.MAX_VALUE);
        }
        this.period = period;
    }

    public synchronized void restoreDefaultQos() {
        this.period = DEFAULT_DEADLINE;
    }


}
