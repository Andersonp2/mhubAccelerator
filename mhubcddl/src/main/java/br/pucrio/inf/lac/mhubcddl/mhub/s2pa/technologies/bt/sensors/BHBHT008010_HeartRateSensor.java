/**
 *
 */
package br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors;

import java.util.UUID;

/**
 * @author bertodetacio
 */
public class BHBHT008010_HeartRateSensor extends BHBHT008010_AbstractSensor {


    private static BHBHT008010_HeartRateSensor instance;

    /**
     *
     */
    public BHBHT008010_HeartRateSensor() {

    }

    public static BHBHT008010_HeartRateSensor getInstance() {
        if (instance == null) {
            instance = new BHBHT008010_HeartRateSensor();
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
        double heartRate = getGeneralPacketInfo().GetHeartRate(bytes);
        Double[] values = new Double[]{heartRate};
        return values;
    }

    @Override
    public int getPacketMsgID() {
        // TODO Auto-generated method stub
        return GP_MSG_ID;
    }

}
