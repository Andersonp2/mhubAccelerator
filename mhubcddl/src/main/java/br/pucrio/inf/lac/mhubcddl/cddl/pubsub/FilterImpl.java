package br.pucrio.inf.lac.mhubcddl.cddl.pubsub;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import br.pucrio.inf.lac.mhubcddl.cddl.message.Message;

/**
 * Created by lcmuniz on 19/02/17.
 */
public class FilterImpl implements Filter {

    private String providerName = "prov"+System.currentTimeMillis();
    private EPServiceProvider epService;
    private EPAdministrator epAdmin;

    private Client callback;

    public FilterImpl(Client callback) {
        this.callback = callback;
        init();
    }

    @Override
    public boolean isSet() {
        return epAdmin.getStatementNames().length != 0;
    }

    private void init() {

        Configuration config = new Configuration();
        config.addEventTypeAutoName(Message.class.getPackage().getName());
        epService = EPServiceProviderManager.getProvider(providerName, config);
        epAdmin = epService.getEPAdministrator();

    }

    @Override
    public void set(String query) {
        query = "select * from SensorDataMessage where " + query;
        clear();
        EPStatement queryEventStatement = epAdmin.createEPL(query);
        queryEventStatement.addListener(new UpdateListener() {
            @Override
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                EventBean event = newEvents[0];
                Message message = (Message) event.getUnderlying();
                callback.send(message, message.getTopic());
            }
        });
    }

    public void clear() {
        epAdmin.destroyAllStatements();
    }

    @Override
    public void process(Message message) {
            epService.getEPRuntime().sendEvent(message);
    }

}
