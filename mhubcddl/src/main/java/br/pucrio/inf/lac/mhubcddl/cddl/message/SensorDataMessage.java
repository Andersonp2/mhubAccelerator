package br.pucrio.inf.lac.mhubcddl.cddl.message;


import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

import br.pucrio.inf.lac.mhubcddl.mhub.models.locals.SensorData;
import br.pucrio.inf.lac.mhubcddl.mhub.models.locals.SensorDataExtended;

public class SensorDataMessage extends Message {

    private String serviceName;
    private byte[] serviceValue;

    public SensorDataMessage() {
        super();
    }

    public SensorDataMessage(SensorData sensorData) {

        this.setServiceName(sensorData.getSensorName());

        // armazena valor do objeto de acordo com o tipo de dado
        if (sensorData instanceof SensorDataExtended) {
            SensorDataExtended extended = (SensorDataExtended) sensorData;
            if (extended.getSensorObjectValue() != null) {
                setServiceValue(((SensorDataExtended) sensorData).getSensorObjectValue());
            }
        }
        else if (sensorData instanceof SensorData) {
            setServiceValue(sensorData.getSensorValue());
        }

    }

    // getters and setters

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Serializable getServiceValue() {
        if (serviceValue == null) return null;
        return SerializationUtils.deserialize(serviceValue);
    }

    public void setServiceValue(Serializable serviceValue) {
        if (serviceValue == null) serviceValue = null;
        this.serviceValue = SerializationUtils.serialize(serviceValue);
    }

}
