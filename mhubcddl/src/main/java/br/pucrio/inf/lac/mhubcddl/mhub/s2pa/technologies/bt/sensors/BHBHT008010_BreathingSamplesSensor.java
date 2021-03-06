/**
 *
 */
package br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors;

import java.util.UUID;

/**
 * @author bertodetacio
 */
public class BHBHT008010_BreathingSamplesSensor extends BHBHT008010_AbstractSensor {


    private static BHBHT008010_BreathingSamplesSensor instance;

    /**
     *
     */
    public BHBHT008010_BreathingSamplesSensor() {
        // TODO Auto-generated constructor stub
    }

    public static BHBHT008010_BreathingSamplesSensor getInstance() {
        if (instance == null) {
            instance = new BHBHT008010_BreathingSamplesSensor();
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
        Double[] values = getBreathingPacketInfo().GetBreathingSamples(bytes);
        return values;
    }

    @Override
    public int getPacketMsgID() {
        // TODO Auto-generated method stub
        return BREATHING_MSG_ID;
    }

}
