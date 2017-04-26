package br.pucrio.inf.lac.mhubcddl.cddl.qos;

public class DurabilityQoS {

    public static final int VOLATILE = 0;

    public static final int PERSISTENT = 1;

    public static final int DEFAULT_KIND = VOLATILE;

    private int kind = VOLATILE;


    public int getKind() {
        return kind;
    }


    public void setKind(int kind) throws IllegalArgumentException {

        if (kind > PERSISTENT || kind < VOLATILE) {
            throw new IllegalArgumentException("O valor não pode ser menor que " + VOLATILE + "e nem maior que " + PERSISTENT);
        }

        this.kind = kind;
    }


    public void restoreDefaultQoS() {
        this.kind = DEFAULT_KIND;
    }

    public boolean isRetained() {
        if (kind == PERSISTENT) {
            return true;
        } else {
            return false;
        }
    }


}
