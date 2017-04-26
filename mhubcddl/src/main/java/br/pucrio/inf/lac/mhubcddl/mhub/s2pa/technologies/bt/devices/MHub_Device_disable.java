/**
 *
 */
package br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.devices;

import java.util.List;
import java.util.UUID;

import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.base.TechnologyDevice;
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.base.TechnologySensor;
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.BTMobileObject;

/**
 * @author bertodetacio
 */
public class MHub_Device_disable extends BTMobileObject implements TechnologyDevice {

    /**
     *
     */
    public MHub_Device_disable() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onReceiveData(byte[] data) {
        // TODO Auto-generated method stub

    }

    @Override
    public void readSensorValue(String sensorName) {
        // TODO Auto-generated method stub

    }

    @Override
    public void writeSensorValue(String sensorName, Object value) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<String> getServices() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TechnologySensor getServiceByName(String serviceName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<TechnologySensor> getServiceByUUID(UUID uuid) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TechnologySensor getCharacteristicByUUID(UUID uuid) {
        return null;
    }

    @Override
    public boolean initialize(Object o) {
        return false;
    }

    @Override
    public boolean loadState(Object o) {
        return false;
    }

    @Override
    public Object getState() {
        return null;
    }

    @Override
    public String getVersion() {
        return "0.1";
    }

}
