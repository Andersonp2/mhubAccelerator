package br.pucrio.inf.lac.mhubcddl.cddl.pubsub;

/**
 * Created by lcmuniz on 05/03/17.
 */
public class CDDLFilterImpl implements CDDLFilter {

    private final String eplFilter;

    public CDDLFilterImpl(String eplFilter) {
        this.eplFilter = "select * from SensorDataMessage where " + eplFilter;
    }

    @Override
    public String getEplFilter() {
        return eplFilter;
    }

}
