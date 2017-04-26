/**
 *
 */
package br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors;

import java.util.UUID;

/**
 * @author bertodetacio
 */
public class BHBHT008010_RtoRSamplesSensor extends BHBHT008010_AbstractSensor {


    private static BHBHT008010_RtoRSamplesSensor instance;

    /**
     *
     */
    public BHBHT008010_RtoRSamplesSensor() {
        // TODO Auto-generated constructor stub
    }

    public static BHBHT008010_RtoRSamplesSensor getInstance() {
        if (instance == null) {
            instance = new BHBHT008010_RtoRSamplesSensor();
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
        int[] RtoRSamples = getRtoRPacketInfo().GetRtoRSamples(bytes);
        Double[] values = new Double[RtoRSamples.length];
        for (int i = 0; i < RtoRSamples.length; i++) {
            values[i] = Double.valueOf(RtoRSamples[i]);
        }
        return values;
    }

    @Override
    public int getPacketMsgID() {
        // TODO Auto-generated method stub
        return RtoR_MSG_ID;
    }

}
