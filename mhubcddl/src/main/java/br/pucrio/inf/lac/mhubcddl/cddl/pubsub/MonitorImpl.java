package br.pucrio.inf.lac.mhubcddl.cddl.pubsub;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import java.util.HashMap;
import java.util.Map;

import br.pucrio.inf.lac.mhubcddl.cddl.listeners.IMonitorListener;
import br.pucrio.inf.lac.mhubcddl.cddl.message.Message;

/**
 * Created by lcmuniz on 19/02/17.
 */
public class MonitorImpl implements Monitor {

    private EPServiceProvider epService;
    private EPAdministrator epAdmin;
    private Map<Long, EPStatement> statements = new HashMap<>();

    public MonitorImpl() {

        init();

    }

    private void init() {
        Configuration config = new Configuration();
        config.addEventTypeAutoName(Message.class.getPackage().getName());
        epService = EPServiceProviderManager.getProvider("prov"+System.currentTimeMillis(),config);
        epAdmin = epService.getEPAdministrator();
    }


    @Override
    public long addQuery(String query, final IMonitorListener monitorListener) {

        query = "select * from SensorDataMessage where " + query;
        EPStatement queryEventStatement = epAdmin.createEPL(query);
        queryEventStatement.addListener(new UpdateListener() {
            @Override
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                EventBean event = newEvents[0];
                Message message = (Message) event.getUnderlying();
                monitorListener.onEvent(message);
            }
        });
        long t = System.currentTimeMillis();
        statements.put(t, queryEventStatement);
        return t;
    }

    public void removeQuery(int id) {
        EPStatement queryEventStatement = statements.get(id);
        queryEventStatement.removeAllListeners();
        statements.remove(id);
    }

    @Override
    public int getNumQueries() {
        return statements.size();
    }

    @Override
    public void messageArrived(Message message) {
            epService.getEPRuntime().sendEvent(message);
    }

}
