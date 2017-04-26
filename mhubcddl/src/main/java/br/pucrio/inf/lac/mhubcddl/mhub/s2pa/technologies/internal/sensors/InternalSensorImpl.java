package br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.internal.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;

import java.util.Arrays;

import br.pucrio.inf.lac.mhubcddl.mhub.models.locals.SensorDataExtended;

/*
 * Device internal sensors that are managed by the system service Context.SENSOR_SERVICE.
 */
public class InternalSensorImpl implements InternalSensor, SensorEventListener, Comparable<InternalSensor> {

    private SensorManager sensorManager;
    private Sensor sensor;

    protected String sensorName;

    private InternalSensorListener listener;
    private int sensorType;
    private int delay;


    /*
     * Constructor that receives the sensor type (class listed in the sensor).
     * For example, Sensor.TYPE_ACCELEROMETER.
     * See other values in http://developer.android.com/reference/android/hardware/Sensor.html
     */
    public InternalSensorImpl(Context context, int sensorType) {
        sensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(sensorType);
        this.sensorType = sensorType;
    }

    /*
     * Start reading the sensor information by registering this class as a listener to the sensor
     * @see br.pucrio.inf.lac.mhub.s2pa.technologies.internal.sensors.InternalSensor#start()
     */
    @Override
    public void start() {
        if (delay == SensorManager.SENSOR_DELAY_FASTEST
                || delay == SensorManager.SENSOR_DELAY_GAME
                || delay == SensorManager.SENSOR_DELAY_NORMAL
                || delay == SensorManager.SENSOR_DELAY_UI) {
            sensorManager.registerListener(this, sensor, delay);
        } else {
            sensorManager.registerListener(this, sensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    /*
     * Stops reading the sensor information by unregistering this class as a listener on the sensor manager
     * @see br.pucrio.inf.lac.mhub.s2pa.technologies.internal.sensors.InternalSensor#stop()
     */
    @Override
    public void stop() {
        sensorManager.unregisterListener(this);
    }

    /*
     * Sets the listener (SensorPhone class)
     * @see br.pucrio.inf.lac.mhub.s2pa.technologies.internal.sensors.InternalSensor#setListener(br.pucrio.inf.lac.mhub.s2pa.technologies.internal.sensors.InternalSensorListener)
     */
    @Override
    public void setListener(InternalSensorListener listener) {
        this.listener = listener;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // NA
    }

    /*
     * Callback method execued by the sensor to submit changes in its values to this class.
     * The data are sent in the event parameter.
     * @see android.hardware.SensorEventListener#onSensorChanged(android.hardware.SensorEvent)
     */
    @Override
    public void onSensorChanged(SensorEvent event) {

        SensorDataExtended data = new SensorDataExtended();

        data.setSensorName(event.sensor.getName());
        data.setSensorObjectValue(event.values);

        data.setAccuracy(event.accuracy);
        data.setMeasurementTime(event.timestamp);
        data.setAvailableAttributes(event.values.length);

        // sends the data to the listener class (SensorPhone)
        listener.onInternalSensorChanged(data);

    }

    /*
     * Return true if sensor exists on device, otherwise return false
     */
    public boolean exists() {
        return sensor != null;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    @Override
    public String getName() {
        return sensor.getName();
    }

    public int getSensorType() {
        return sensorType;
    }

    @Override
    public int compareTo(InternalSensor sensor) {
        return getName().compareTo(sensor.getName());
    }


}
