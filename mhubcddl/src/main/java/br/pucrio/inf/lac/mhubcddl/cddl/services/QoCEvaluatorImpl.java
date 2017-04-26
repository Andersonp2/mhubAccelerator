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

import br.pucrio.inf.lac.mhubcddl.cddl.message.Message;
import br.pucrio.inf.lac.mhubcddl.cddl.message.SensorDataMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.pubsub.CDDLFilter;
import br.pucrio.inf.lac.mhubcddl.cddl.pubsub.Publisher;
import br.pucrio.inf.lac.mhubcddl.cddl.util.Provider;
import br.pucrio.inf.lac.mhubcddl.mhub.models.locals.SensorData;
import br.pucrio.inf.lac.mhubcddl.mhub.models.locals.SensorDataExtended;

/**
 * Created by lcmuniz on 05/03/17.
 */
public class QoCEvaluatorImpl implements QoCEvaluator {

    public static final String TAG = QoCEvaluatorImpl.class.getSimpleName();

    private EPServiceProvider epService;
    private EPAdministrator epAdmin;

    private EPStatement epsStatementServiceInformationMessages;
    private EPStatement epsStatementSensorDataMessages;

    private Publisher publisher;

    private final String TIME_WINDOW = "5 sec";

    private final CDDLFilter defaultCDDLFilter = Provider.newCDDLFilter("select * from SensorDataMessage");

    private CDDLFilter cddlFilter = defaultCDDLFilter;

    public QoCEvaluatorImpl() {

        EventBus.getDefault().register(this);

        final Configuration config = new Configuration();
        config.addEventTypeAutoName(Message.class.getPackage().getName());

        epService = EPServiceProviderManager.getProvider(TAG, config);
        epAdmin = epService.getEPAdministrator();

        createStatementForServiceInformationMessages();

        createStatementForSensorDataMessages();

        publisher = Provider.newPublisher();

    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onMessageEvent(SensorData sensorData) {

        SensorDataMessage sensorDataMessage = evaluateQoC(sensorData);

        epService.getEPRuntime().sendEvent(sensorDataMessage);

    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onMessageEvent(CDDLFilter cddlFilter) {
        if (cddlFilter.equals("")) {
            this.cddlFilter = defaultCDDLFilter;
        }
        else {
            this.cddlFilter = cddlFilter;
        }

    }

    /**
     * calcula parametros de qoc
     * @param sensorData
     * @return uma mensagem SensorDataMessage
     */
    private SensorDataMessage evaluateQoC(SensorData sensorData) {

        SensorDataMessage sensorDataMessage = new SensorDataMessage(sensorData);

        if (sensorData instanceof SensorDataExtended) {
            SensorDataExtended extended = (SensorDataExtended) sensorData;

            sensorDataMessage.setAccuracy(extended.getAccuracy());
            sensorDataMessage.setMeasurementTime(extended.getMeasurementTime());
            sensorDataMessage.setAvailableAttributes(extended.getAvailableAttributes());
            sensorDataMessage.setSourceLocationLatitude(extended.getSourceLocation().getLatitude());
            sensorDataMessage.setSourceLocationLongitude(extended.getSourceLocation().getLongitude());
            sensorDataMessage.setSourceLocationAltitude(extended.getSourceLocation().getAltitude());

        }

        return sensorDataMessage;

    }

    /**
     * calcula a media das qoc e envia para o fluxo de eventos de ServiceInformationMessage
     */
    private void createStatementForServiceInformationMessages() {
        final String epl = "insert into ServiceInformationMessage(publisherID, serviceName, accuracy, measurementTime, availableAttributes, sourceLocationLatitude, sourceLocationLongitude, sourceLocationAltitude, measurementInterval, numericalResolution, age) select publisherID, serviceName, avg(accuracy), avg(measurementTime), avg(availableAttributes), avg(sourceLocationLatitude), avg(sourceLocationLongitude), avg(sourceLocationAltitude), avg(measurementInterval), avg(numericalResolution), avg(age) from SensorDataMessage.win:time(" + TIME_WINDOW + ") group by publisherID, serviceName";
        epsStatementServiceInformationMessages = epAdmin.createEPL(epl);
        epsStatementServiceInformationMessages.start();
    }

    private void createStatementForSensorDataMessages() {

        if (epsStatementSensorDataMessages != null) {
            epsStatementSensorDataMessages.removeAllListeners();
            epsStatementSensorDataMessages.destroy();
        }

        epsStatementSensorDataMessages = epAdmin.createEPL(cddlFilter.getEplFilter());
        epsStatementSensorDataMessages.addListener(new UpdateListener() {
            @Override
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                for (final EventBean e : newEvents) {
                    final SensorDataMessage sensorDataMessage = (SensorDataMessage) e.getUnderlying();
                    publisher.publish(sensorDataMessage);
                    //AppUtils.logger('d', TAG, sensorDataMessage.toString());
                }
            }

        });

    }

}
