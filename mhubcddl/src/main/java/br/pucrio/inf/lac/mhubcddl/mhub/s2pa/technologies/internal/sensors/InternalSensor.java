package br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.internal.sensors;

public interface InternalSensor {

    public void start();

    public void stop();

    public void setListener(InternalSensorListener listener);

    public String getName();

}
