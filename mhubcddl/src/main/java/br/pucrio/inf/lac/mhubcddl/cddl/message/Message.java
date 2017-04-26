package br.pucrio.inf.lac.mhubcddl.cddl.message;


import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.Serializable;
import java.util.UUID;

import br.pucrio.inf.lac.mhubcddl.cddl.listeners.IPublisherListener;

public class Message extends MqttMessage implements Serializable {

    public static final String TAG = Message.class.getName();

    private String className;

    private long expirationTime = 0;

    private long publicationTimestamp;

    private String publisherID;

    private long receptionTimestamp;

    private String topic;

    private String uuid = UUID.randomUUID().toString();

    private String jason;

    private IPublisherListener publisherListener;


    public Message() {
        className = getClass().getName();
    }

    public static Message convertFromPayload(byte[] payload) {
        return new Gson().fromJson(new String(payload), Message.class);
    }

    public static Message convertFromPayload(byte[] payload, Class clazz) {
        return (Message) new Gson().fromJson(new String(payload), clazz);
    }

    public static Message convertFromObject(Object object, Class clazz){
        return (Message) new Gson().fromJson(object.toString(), clazz);
    }

    // getters and setters

    public String getClassName() {
        return className;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    //public long getMeasurementTime() {
    //    return measurementTime;
    //}

    //public void setMeasurementTime(long measurementTime) {
    //    this.measurementTime = measurementTime;
    //}

    public long getPublicationTimestamp() {
        return publicationTimestamp;
    }

    public void setPublicationTimestamp(long publicationTimestamp) {
        this.publicationTimestamp = publicationTimestamp;
    }

    public String getPublisherID() {
        return publisherID;
    }

    public void setPublisherID(String publisherID) {
        this.publisherID = publisherID;
    }

    public long getReceptionTimestamp() {
        return receptionTimestamp;
    }

    public void setReceptionTimestamp(long receptionTimestamp) {
        this.receptionTimestamp = receptionTimestamp;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getUuid() {
        return uuid;
    }

    // tostring, equals and hashcode


    public String toJson(){
        if(jason == null){
            Gson gson = new Gson();
            jason = gson.toJson(this);
        }
        return jason;
    }

    public String toString(){
        return toJson();
    }


    @Override
    public boolean equals(Object other) {

        if (other == null) {
            return false;
        } else if (other instanceof Message) {

            Message message = (Message) other;

            return this.uuid.equalsIgnoreCase(message.getUuid());

        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        if (uuid != null) {
            return uuid.hashCode();
        } else {
            return super.hashCode();
        }
    }

    public IPublisherListener getPublisherListener() {
        return publisherListener;
    }

    public void setPublisherListener(IPublisherListener publisherListener) {
        this.publisherListener = publisherListener;
    }

    // qoc attributes

    private double accuracy;
    private long measurementTime;
    private int availableAttributes;

    private double sourceLocationLatitude;
    private double sourceLocationLongitude;
    private double sourceLocationAltitude;

    // falta fazer
    private long measurementInterval;
    private int numericalResolution;

    // calculated qoc
    public long getAge() {
        return System.currentTimeMillis() - measurementTime;
    }

    // getters and setters

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public long getMeasurementTime() {
        return measurementTime;
    }

    public void setMeasurementTime(long measurementTime) {
        this.measurementTime = measurementTime;
    }

    public void setAvailableAttributes(int availableAttributes) {
        this.availableAttributes = availableAttributes;
    }

    public int getAvailableAttributes() {
        return availableAttributes;
    }

    public long getMeasurementInterval() {
        return measurementInterval;
    }

    public void setMeasurementInterval(long measurementInterval) {
        this.measurementInterval = measurementInterval;
    }

    public int getNumericalResolution() {
        return numericalResolution;
    }

    public void setNumericalResolution(int numericalResolution) {
        this.numericalResolution = numericalResolution;
    }

    public double getSourceLocationLatitude() {
        return sourceLocationLatitude;
    }

    public void setSourceLocationLatitude(double sourceLocationLatitude) {
        this.sourceLocationLatitude = sourceLocationLatitude;
    }

    public double getSourceLocationLongitude() {
        return sourceLocationLongitude;
    }

    public void setSourceLocationLongitude(double sourceLocationLongitude) {
        this.sourceLocationLongitude = sourceLocationLongitude;
    }

    public double getSourceLocationAltitude() {
        return sourceLocationAltitude;
    }

    public void setSourceLocationAltitude(double sourceLocationAltitude) {
        this.sourceLocationAltitude = sourceLocationAltitude;
    }
}
