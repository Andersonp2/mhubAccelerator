package br.pucrio.inf.lac.mhubcddl.cddl.qos;


public class HistoryQoS extends AbstractQoS {

    public static final long DEFAULT_DEPTH = 0;

    private long depth = DEFAULT_DEPTH;

    public static final int KEEP_ALL = 0;

    public static final int KEEP_LAST = 1;

    private static final int DEFAULT_KIND = KEEP_LAST;

    private int kind = DEFAULT_KIND;

    public long getDepth() {
        return depth;
    }

    public void setDepth(long depth) throws IllegalArgumentException {
        if (depth < 0) {
            throw new IllegalArgumentException("Tamanho do Array Inválido. Não pode ser negativo ");
        }
        this.depth = depth;
    }

    public int getKind() {
        return kind;
    }

    public void setKind(int kind) {
        this.kind = kind;
    }

    public void restoreDefaultQoS() {
        this.kind = DEFAULT_KIND;
        this.depth = DEFAULT_DEPTH;
    }


}
