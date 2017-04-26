/**
 *
 */
package br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.devices;

import android.util.Log;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.Vector;

import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.base.TechnologyDevice;
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.base.TechnologySensor;
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.BTMobileObject;
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors.HXM030655_AbstractSensor;
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors.HXM030655_DistanceSensor;
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors.HXM030655_HeartRateSensor;
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors.HXM030655_InstantSpeedSensor;
import zephyr.android.HxMBT.ZephyrPacket;
import zephyr.android.HxMBT.ZephyrPacketArgs;

/**
 * @author bertodetacio
 */
public class HXM030655_Device extends BTMobileObject implements TechnologyDevice {


    //faz o mapeamento de um sensor para cada serviço
    private Hashtable<String, HXM030655_AbstractSensor> sensors = new Hashtable<String, HXM030655_AbstractSensor>();

    //tabela de informações de contexto (considera sempre a última leitura)
    private Hashtable<String, Double[]> contextInformations = new Hashtable<String, Double[]>();

    //Timer Android
    private Timer timer = new Timer();

    //Descobre os serviços e comunica o listener periodicamente
    private TimerTaskDiscoveryServices timerTaskDiscoveryServices = new TimerTaskDiscoveryServices();

    //tempo entre uma divulgação e outra de serviços descobertos
    private long timeDiscoveryServices = 30000;

    //serializador e parser de dados do zephyr
    ZephyrPacket zephyrPacket = new ZephyrPacket();

    //tipo de monitoramento. False indica é necessário invocar o o metodo read para receber dados
    private boolean activeMonitoring = true;

    /**
     *
     */
    public HXM030655_Device() {
        super();
        init();
    }


    private void init() {
        inicializeSensors();
        timer.scheduleAtFixedRate(timerTaskDiscoveryServices, timeDiscoveryServices, timeDiscoveryServices);
    }


    @Override
    public TechnologySensor getServiceByName(String serviceName) {
        return sensors.get(serviceName);
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

    public void addSensor(HXM030655_AbstractSensor sensor) {
        sensors.put(sensor.getName(), sensor);
    }

    public void removeSensor(TechnologySensor sensor) {
        sensors.remove(sensor);
    }

    public void removeSensor(String name) {
        sensors.remove(name);
    }

    @Override
    public List<String> getServices() {
        List<String> services = new ArrayList<String>(sensors.keySet());
        return services;
    }


    public synchronized void onReceiveData(byte[] data) {

        Vector<byte[]> serializedDataVector = serialize(data);
        for (byte[] serializedData : serializedDataVector) {
            ZephyrPacketArgs zephyrPacketArgs = parser(serializedData);
            if (validate(zephyrPacketArgs)) {
                interpretAndUpdateContextInformations(zephyrPacketArgs);
            }
        }


    }

    private Vector<byte[]> serialize(byte[] data) {
        Vector<byte[]> packets = zephyrPacket.Serialize(data);
        return packets;
    }

    private ZephyrPacketArgs parser(byte[] serializedData) {
        ZephyrPacketArgs zephyrPacketArgs = null;
        try {
            zephyrPacketArgs = zephyrPacket.Parse(serializedData);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return zephyrPacketArgs;
    }

    private boolean validate(ZephyrPacketArgs zephyrPacketArgs) {
        if (zephyrPacketArgs != null && zephyrPacketArgs.getNumRvcdBytes() > 1) {
            return true;
        } else {
            return false;
        }
    }

    private void inicializeSensors() {
        addSensor(HXM030655_DistanceSensor.getInstance());
        addSensor(HXM030655_HeartRateSensor.getInstance());
        addSensor(HXM030655_InstantSpeedSensor.getInstance());
    }


    private void interpretAndUpdateContextInformations(ZephyrPacketArgs zephyrPacketArgs) {
        byte[] data = zephyrPacketArgs.getBytes();
        for (HXM030655_AbstractSensor sensor : sensors.values()) {
            String contextInformation = sensor.getName();
            int packetMsgID = sensor.getPacketMsgID();
            if (zephyrPacketArgs.getMsgID() == packetMsgID) {
                Double[] values = null;
                try {
                    values = sensor.convert(data);
                    contextInformations.put(contextInformation, values);
                } catch (Exception e) {
                    Log.e("Log", "Conversion error for " + contextInformation);
                }
                if (activeMonitoring) {
                    onValueRead(contextInformation, values);
                }
            }
        }
    }


    private class TimerTaskDiscoveryServices extends TimerTask {
        @Override
        public void run() {
            if (isConnected()) {
                List<String> services = getServices();
                onServiceDiscovered(services);
            }
        }
    }


    @Override
    public void readSensorValue(String serviceName) {
        if (contextInformations.containsKey(serviceName)) {
            Double[] values = contextInformations.get(serviceName);
            onValueRead(serviceName, values);
        }
    }

    @Override
    public void writeSensorValue(String sensorName, Object value) {
        // TODO Auto-generated method stub

    }

    public boolean isActiveMonitoring() {
        return activeMonitoring;
    }

    public void setActiveMonitoring(boolean activeMonitoring) {
        this.activeMonitoring = activeMonitoring;
    }

    public long getTimeDiscoveryServices() {
        return timeDiscoveryServices;
    }

    public void setTimeDiscoveryServices(long timeDiscoveryServices) {
        this.timeDiscoveryServices = timeDiscoveryServices;
        if (timer != null && timerTaskDiscoveryServices != null) {
            timer.cancel();
            timer.scheduleAtFixedRate(timerTaskDiscoveryServices, timeDiscoveryServices, timeDiscoveryServices);
        }
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
