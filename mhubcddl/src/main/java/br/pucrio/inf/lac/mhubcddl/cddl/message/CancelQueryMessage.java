package br.pucrio.inf.lac.mhubcddl.cddl.message;

/**
 * Created by lcmuniz on 09/03/17.
 */

public class CancelQueryMessage {
    private final long returnCode;

    public CancelQueryMessage(long returnCode) {
        this.returnCode = returnCode;
    }

    public long getReturnCode() {
        return returnCode;
    }
}
