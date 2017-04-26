package br.pucrio.inf.lac.mhubcddl.cddl.qos;

public class TimeBasedFilterQoS extends AbstractQoS {

    public static final long DEFAULT_MIN_SEPARATION_INTERVAL = 0;

    private long minSeparation = DEFAULT_MIN_SEPARATION_INTERVAL;

    public long getMinSeparation() {
        return minSeparation;
    }

    public void setMinSeparation(long minSeparation) {
        if (minSeparation > Long.MAX_VALUE || minSeparation < DEFAULT_MIN_SEPARATION_INTERVAL) {
            throw new IllegalArgumentException("O valor deve não pode ser maior que " + Long.MAX_VALUE + " e nem menor que " + DEFAULT_MIN_SEPARATION_INTERVAL);
        }
        this.minSeparation = minSeparation;
    }

    public void restoreDefaultQoS() {
        this.minSeparation = DEFAULT_MIN_SEPARATION_INTERVAL;
    }
}
