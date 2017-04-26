/**
 *
 */
package br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors;

import java.util.UUID;

/**
 * @author bertodetacio
 */
public class BHBHT008010_ECGAmplitudeSensor extends BHBHT008010_AbstractSensor {


    private static BHBHT008010_ECGAmplitudeSensor instance;

    /**
     *
     */
    public BHBHT008010_ECGAmplitudeSensor() {
        // TODO Auto-generated constructor stub
    }

    public static BHBHT008010_ECGAmplitudeSensor getInstance() {
        if (instance == null) {
            instance = new BHBHT008010_ECGAmplitudeSensor();
        }
        return instance;
    }

    @Override
    public UUID getCalibration() {
        return null;
    }

    @Override
    public void setCalibrationData(byte[] value) throws UnsupportedOperationException {

    }

    @Override
    public Double[] convert(byte[] bytes) {
        double ecgAmplitude = getGeneralPacketInfo().GetECGAmplitude(bytes);
        Double[] values = new Double[]{ecgAmplitude};
        return values;
    }

    @Override
    public int getPacketMsgID() {
        // TODO Auto-generated method stub
        return GP_MSG_ID;
    }

}
