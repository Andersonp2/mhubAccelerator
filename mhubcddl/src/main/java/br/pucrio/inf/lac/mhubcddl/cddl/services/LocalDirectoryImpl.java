package br.pucrio.inf.lac.mhubcddl.cddl.services;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import br.pucrio.inf.lac.mhubcddl.cddl.message.CancelQueryMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.message.Message;
import br.pucrio.inf.lac.mhubcddl.cddl.message.QueryMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.message.QueryResponseMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.message.ServiceInformationMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.ontology.QueryType;
import br.pucrio.inf.lac.mhubcddl.cddl.pubsub.Publisher;
import br.pucrio.inf.lac.mhubcddl.cddl.util.Provider;

/**
 * Created by lcmuniz on 05/03/17.
 */
public class LocalDirectoryImpl implements LocalDirectory {

    private static final String TAG = LocalDirectoryImpl.class.getSimpleName();

    private EPServiceProvider epService;
    private EPAdministrator epAdmin;

    private final String TIME_WINDOW = "5 sec";

    private Publisher publisher;

    public LocalDirectoryImpl() {

        EventBus.getDefault().register(this);

        final Configuration config = new Configuration();
        config.addEventTypeAutoName(Message.class.getPackage().getName());

        // pega o mesmo provider do QoCEvaluator
        epService = EPServiceProviderManager.getProvider(QoCEvaluatorImpl.TAG, config);
        epAdmin = epService.getEPAdministrator();

        publisher = Provider.newPublisher();

    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onMessageEvent(final QueryMessage queryMessage) {

            processQuery(queryMessage);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onMessageEvent(final CancelQueryMessage cancelQueryMessage) {

        cancelQuery(cancelQueryMessage);

    }

    private void processQuery(final QueryMessage queryMessage) {

        final String epl = "select * from ServiceInformationMessage.win:time_batch(" + TIME_WINDOW + ") where " + queryMessage.getQuery();
        final EPStatement queryEventStatement = epAdmin.createEPL(epl, ""+queryMessage.getReturnCode());
        queryEventStatement.addListener(new UpdateListener() {
            @Override
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                final QueryResponseMessage qrm = new QueryResponseMessage(queryMessage);
                qrm.setSubscriberID(queryMessage.getPublisherID());

                for (EventBean e : newEvents) {
                    ServiceInformationMessage sim = new ServiceInformationMessage();
                    sim.setPublisherID((String) e.get("publisherID"));
                    sim.setServiceName((String) e.get("serviceName"));
                    sim.setAccuracy((double) e.get("accuracy"));
                    sim.setMeasurementTime(new Double((double) e.get("measurementTime")).longValue());
                    sim.setAvailableAttributes(new Double((double) e.get("availableAttributes")).intValue());
                    sim.setSourceLocationLatitude((double) e.get("sourceLocationLatitude"));
                    sim.setSourceLocationLongitude((double) e.get("sourceLocationLongitude"));
                    sim.setSourceLocationAltitude((double) e.get("sourceLocationAltitude"));
                    sim.setMeasurementInterval(new Double((double) e.get("measurementInterval")).longValue());
                    sim.setNumericalResolution(new Double((double) e.get("numericalResolution")).intValue());
                    sim.setAge(new Double((double) e.get("age")).longValue());
                    qrm.getServiceInformationMessageList().add(sim);
                }
                publisher.publish(qrm);
                //AppUtils.logger('d', TAG, qrm.toString());

                if (queryMessage.getType() == QueryType.SIMPLE) {
                    queryEventStatement.removeAllListeners();
                    queryEventStatement.destroy();
                }

            }

        });

    }

    private void cancelQuery(CancelQueryMessage cancelQueryMessage) {
        final EPStatement queryEventStatement = epAdmin.getStatement(""+cancelQueryMessage.getReturnCode());
        queryEventStatement.removeAllListeners();
        queryEventStatement.destroy();
    };

}



