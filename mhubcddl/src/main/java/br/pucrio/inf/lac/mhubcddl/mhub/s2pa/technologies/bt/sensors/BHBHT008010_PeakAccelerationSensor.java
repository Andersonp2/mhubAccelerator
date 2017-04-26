/**
 *
 */
package br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors;

import java.util.UUID;

/**
 * @author bertodetacio
 */
public class BHBHT008010_PeakAccelerationSensor extends BHBHT008010_AbstractSensor {


    private static BHBHT008010_PeakAccelerationSensor instance;

    /**
     *
     */
    public BHBHT008010_PeakAccelerationSensor() {
        // TODO Auto-generated constructor stub
    }

    public static BHBHT008010_PeakAccelerationSensor getInstance() {
        if (instance == null) {
            instance = new BHBHT008010_PeakAccelerationSensor();
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
        double peakAccelerationSensor = getGeneralPacketInfo().GetPeakAcceleration(bytes);
        Double[] values = new Double[]{peakAccelerationSensor};
        return values;
    }

    @Override
    public int getPacketMsgID() {
        // TODO Auto-generated method stub
        return GP_MSG_ID;
    }

}
