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
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors.BHBHT008010_AbstractSensor;
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors.BHBHT008010_BatteryLevelSensor;
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors.BHBHT008010_BatteryStatusSensor;
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors.BHBHT008010_BreathingSamplesSensor;
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors.BHBHT008010_BreathingWaveAmplitudeSensor;
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors.BHBHT008010_ECGAmplitudeSensor;
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors.BHBHT008010_ECGNoiseSensor;
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors.BHBHT008010_ECGSamplesSensor;
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors.BHBHT008010_HeartRateSensor;
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors.BHBHT008010_PeakAccelerationSensor;
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors.BHBHT008010_PostureSensor;
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors.BHBHT008010_RespirationRateSensor;
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors.BHBHT008010_RtoRSamplesSensor;
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors.BHBHT008010_SkinTemperatureSensor;
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors.BHBHT008010_XYZAxesAccelerationDataSensor;
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors.BHBHT008010_XYZAxesMinAccelerationSensor;
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors.BHBHT008010_XYZAxesPeakAccelerationSensor;
import zephyr.android.BioHarnessBT.ZephyrPacket;
import zephyr.android.BioHarnessBT.ZephyrPacketArgs;

/**
 * @author bertodetacio
 */
public class BHBHT008010_Device extends BTMobileObject implements TechnologyDevice {

    //faz o mapeamento de um sensor para cada serviço
    private Hashtable<String, BHBHT008010_AbstractSensor> sensors = new Hashtable<String, BHBHT008010_AbstractSensor>();

    //tabela de informações de contexto (considera sempre a última leitura)
    private Hashtable<String, Double[]> contextInformations = new Hashtable<String, Double[]>();

    //Timer Android
    private Timer timer = new Timer();

    //Descobre os serviços e comunica o listener periodicamente
    private DiscoveryServicesTimerTask discoveryServicesTimerTask = new DiscoveryServicesTimerTask();

    //envia sinais de vida a fim de manter o link da conexão
    private SendSignalLifeTimerTask sendSignalLifeTimerTask = new SendSignalLifeTimerTask();

    //tempo entre uma divulgação e outra de serviços descobertos
    private long timeDiscoveryServices = 30000;

    //entre entre envios de sinais de vida
    private long timeBetweenSendingOutSignsofLife = 500;

    //serializador e parser de dados do zephyr
    ZephyrPacket zephyrPacket = new ZephyrPacket();

    //tipo de monitoramento. False indica é necessário invocar o o metodo read para receber dados
    private boolean activeMonitoring = true;

    /**
     *
     */
    public BHBHT008010_Device() {
        super();
        init();
    }


    private void init() {
        inicializeSensors();
        requestPackets();
        timer.scheduleAtFixedRate(discoveryServicesTimerTask, timeDiscoveryServices, timeDiscoveryServices);
        timer.scheduleAtFixedRate(sendSignalLifeTimerTask, 0, timeBetweenSendingOutSignsofLife);
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
        if (zephyrPacketArgs != null && zephyrPacketArgs.getNumRvcdBytes() > 0) {
            return true;
        } else {
            return false;
        }
    }


    private void interpretAndUpdateContextInformations(ZephyrPacketArgs zephyrPacketArgs) {
        byte[] data = zephyrPacketArgs.getBytes();
        for (BHBHT008010_AbstractSensor sensor : sensors.values()) {
            String contextInformation = sensor.getName();
            int packetMsgID = sensor.getPacketMsgID();
            if (zephyrPacketArgs.getMsgID() == packetMsgID) {
                Double[] values = null;
                try {
                    values = sensor.convert(data);
                    contextInformations.put(contextInformation, values);
                    if (activeMonitoring) {
                        onValueRead(contextInformation, values);
                    }
                } catch (Exception e) {
                    Log.e("Log", "Conversion error for " + contextInformation);
                }

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
    }

    public long getTimeBetweenSendingOutSignsofLife() {
        return timeBetweenSendingOutSignsofLife;
    }

    public void setTimeBetweenSendingOutSignsofLife(
            long timeBetweenSendingOutSignsofLife) {
        this.timeBetweenSendingOutSignsofLife = timeBetweenSendingOutSignsofLife;
    }

    public void addSensor(BHBHT008010_AbstractSensor sensor) {
        sensors.put(sensor.getName(), sensor);
    }

    public void removeSensor(BHBHT008010_AbstractSensor sensor) {
        sensors.remove(sensor);
    }

    public void removeSensor(String name) {
        sensors.remove(name);
    }

    private void requestPackets() {
        write(zephyrPacket.getSetGeneralPacketMessage(true));
        write(zephyrPacket.getSetAccelerometerPacketMessage(true));
        write(zephyrPacket.getSetBreathingPacketMessage(true));
        write(zephyrPacket.getSetECGPacketMessage(true));
        write(zephyrPacket.getSetRtoRPacketMessage(true));
        write(zephyrPacket.getSetSummaryPacketMessage(true));
    }

    private void inicializeSensors() {
        addSensor(BHBHT008010_HeartRateSensor.getInstance());
        addSensor(BHBHT008010_PeakAccelerationSensor.getInstance());
        addSensor(BHBHT008010_RespirationRateSensor.getInstance());
        addSensor(BHBHT008010_SkinTemperatureSensor.getInstance());
        addSensor(BHBHT008010_XYZAxesPeakAccelerationSensor.getInstance());
        addSensor(BHBHT008010_XYZAxesMinAccelerationSensor.getInstance());
        addSensor(BHBHT008010_PostureSensor.getInstance());
        addSensor(BHBHT008010_BreathingWaveAmplitudeSensor.getInstance());
        addSensor(BHBHT008010_ECGAmplitudeSensor.getInstance());
        addSensor(BHBHT008010_ECGNoiseSensor.getInstance());
        addSensor(BHBHT008010_XYZAxesAccelerationDataSensor.getInstance());
        addSensor(BHBHT008010_BreathingSamplesSensor.getInstance());
        addSensor(BHBHT008010_ECGSamplesSensor.getInstance());
        addSensor(BHBHT008010_RtoRSamplesSensor.getInstance());
        addSensor(BHBHT008010_BatteryStatusSensor.getInstance());
        addSensor(BHBHT008010_BatteryLevelSensor.getInstance());
    }


    private class DiscoveryServicesTimerTask extends TimerTask {
        @Override
        public void run() {
            if (isConnected()) {
                List<String> services = getServices();
                onServiceDiscovered(services);
            }

        }
    }

    private class SendSignalLifeTimerTask extends TimerTask {

        @Override
        public void run() {
            if (zephyrPacket != null) {
                if (isConnected()) {
                    byte[] signalLife = zephyrPacket.getLifeSignMessage();
                    write(signalLife);
                }
            }
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
