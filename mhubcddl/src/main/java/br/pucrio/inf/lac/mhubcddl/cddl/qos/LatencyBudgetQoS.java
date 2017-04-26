package br.pucrio.inf.lac.mhubcddl.cddl.qos;

public class LatencyBudgetQoS {

    public static final long DEFAULT_DELAY = 0;

    private long delay = DEFAULT_DELAY;


    public long getDelay() {
        return delay;
    }


    public void setDelay(long delay) {

        if (delay > Long.MAX_VALUE || delay < 0) {
            throw new IllegalArgumentException("O valor não pode ser maior que " + Long.MAX_VALUE + " e nem menor que " + DEFAULT_DELAY);
        }

        this.delay = delay;
    }

    public void restoreDefaultQoS() {
        this.delay = DEFAULT_DELAY;
    }

}
