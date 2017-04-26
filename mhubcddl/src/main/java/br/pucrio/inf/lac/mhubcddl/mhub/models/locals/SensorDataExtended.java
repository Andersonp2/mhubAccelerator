package br.pucrio.inf.lac.mhubcddl.mhub.models.locals;

import android.location.Location;

import com.google.gson.Gson;

import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

import javax.xml.transform.Source;

/**
 * Contains the sensor data received from the
 * Mobile Objects
 */
public class SensorDataExtended extends SensorData {
    /**
     * DEBUG
     */
    private static final String TAG = SensorDataExtended.class.getSimpleName();

    public SensorDataExtended() {
        super(TAG);
    }

    private Serializable sensorObjectValue;

    private double accuracy;
    private long measurementTime;
    private int availableAttributes;
    private SourceLocation sourceLocation = new SourceLocation();

    public Serializable getSensorObjectValue() {
        return sensorObjectValue;
    }

    public void setSensorObjectValue(Serializable sensorObjectValue) {
        this.sensorObjectValue = sensorObjectValue;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public int getAvailableAttributes() {
        return availableAttributes;
    }

    public void setAvailableAttributes(int availableAttributes) {
        this.availableAttributes = availableAttributes;
    }

    public long getMeasurementTime() {
        return measurementTime;
    }

    public void setMeasurementTime(long measurementTime) {
        this.measurementTime = measurementTime;
    }

    public SourceLocation getSourceLocation() {
        return sourceLocation;
    }

    public void setSourceLocation(SourceLocation sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
