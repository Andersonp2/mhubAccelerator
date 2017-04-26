package br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.internal.devices;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import br.pucrio.inf.lac.mhubcddl.mhub.components.MOUUID;
import br.pucrio.inf.lac.mhubcddl.mhub.models.locals.SensorDataExtended;
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.base.TechnologyListener;
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.internal.sensors.BatterySensor;
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.internal.sensors.InternalSensor;
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.internal.sensors.InternalSensorImpl;
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.internal.sensors.InternalSensorListener;
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.internal.sensors.LocationSensor;

/*
 * SensorPhone represents the device containing the M-Hub (usually a cellphone).
 * It uses a list of sensors for which registers itself as a listener and receives data. 
 * This class is used by InternalTechnology class to maintain a connection to the device's sensors. 
 */
public class SensorPhone implements InternalSensorListener {

    public static final String DEVICE_NAME = "SensorPhone";

    private MOUUID device = null;

    // LCMUNIZ - atualizar intervalo e verificar se nao pode ser configuravel
    private static final long BATTERY_READ_INTERVAL = 1000 * 60; // battery level reading
    // interval, in
    // milliseconds

    private Double mRssi = 0.0;
    private String macAddress;  // This MAC address by convention represents the device
    private MOUUID mouuid;
    TechnologyListener listener;

    public List<InternalSensor> sensors;

    private Context context;

    /*
     * Constructor. Receives the listener (the S2PAService) and the id of the
     * technology Calls addSensors to initialize the internal sensors of the
     * device
     */
    public SensorPhone(Context context, int id, TechnologyListener listener) {
        this.context = context;
        macAddress = getMAC();
        mouuid = new MOUUID(id, macAddress);
        this.listener = listener;

        sensors = new ArrayList<InternalSensor>();
        addSensors();

    }

    public void scan() {

    }

    /*
     * Adds the internal sensor of the device and sets this class as the
     * listener for changes on sensor's values
     */
    private void addSensors() {
        List<String> listAvailableServices = new ArrayList<String>();
        listener.onMObjectConnected(mouuid);

        listAvailableServices.add(LocationSensor.NAME);
        listAvailableServices.add(BatterySensor.NAME);

        sensors.add(new LocationSensor(context));
        sensors.add(new BatterySensor(context, BATTERY_READ_INTERVAL));

        SensorManager mSensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);

        List<Sensor> sensList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Iterator<Sensor> iterator = sensList.iterator(); iterator
                .hasNext(); ) {
            Sensor sensor = (Sensor) iterator.next();
            int idType = sensor.getType();
            listAvailableServices.add(sensor.getName());

            addSensor(idType, 5000);

        }

        for (InternalSensor internalSensor : sensors) {
            internalSensor.setListener(this);
        }
        listener.onMObjectServicesDiscovered(mouuid, listAvailableServices);
    }

    /*
     * Adds sensor to list of sensors if it exists
     */
    private void addSensor(int sensorType, Integer delay) {
        InternalSensorImpl is = new InternalSensorImpl(context, sensorType);

        if (delay != null) {
            is.setDelay(delay);
        }
        if (is.exists()) {
            sensors.add(is);
        } else {
            is = null;
        }
    }

    /*
     * Inform the service that the device was found and connected Stars all the
     * sensors
     */
    public void enable() {
        listener.onMObjectFound(mouuid, mRssi);
        listener.onMObjectConnected(mouuid);
        startSensors();
    }

    /*
     * Stops all sensors Inform the service that the object was disconnected
     */
    public void disable() {
        stopSensors();
        List<String> services = new ArrayList<String>();

        listener.onMObjectDisconnected(mouuid, services);
    }

    /*
     * Callback method to receive updates on sensor's data Inform the service
     * the change and pass others informations
     */
    @Override
    public void onInternalSensorChanged(SensorDataExtended data) {
        listener.onMObjectValueRead(mouuid, mRssi, data.getSensorName(), data);
    }

    /*
     * Starts all sensors;
     */
    private void startSensors() {
        for (InternalSensor internalSensor : sensors) {
            internalSensor.start();
        }
    }

    /*
     * Stops all sensors
     */
    private void stopSensors() {
        List<String> list = new ArrayList<String>();
        for (InternalSensor internalSensor : sensors) {
            internalSensor.stop();
            list.add(internalSensor.getName());
        }
        listener.onMObjectDisconnected(mouuid, list);
    }

    private String getMAC() {
        try {
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = manager.getConnectionInfo();
            String address = info.getMacAddress();
            if (address == null) address = "00:00:00:00:00:00";
            return address;
        } catch (Exception e) {
            e.printStackTrace();
            return "00:00:00:00:00:00";
        }
    }


}
