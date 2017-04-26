/**
 *
 */
package br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors;

import java.util.UUID;

/**
 * @author bertodetacio
 */
public class BHBHT008010_BatteryStatusSensor extends BHBHT008010_AbstractSensor {


    private static BHBHT008010_BatteryStatusSensor instance;

    /**
     *
     */
    public BHBHT008010_BatteryStatusSensor() {
        // TODO Auto-generated constructor stub
    }

    public static BHBHT008010_BatteryStatusSensor getInstance() {
        if (instance == null) {
            instance = new BHBHT008010_BatteryStatusSensor();
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
        double batteryStatus = getGeneralPacketInfo().GetBatteryStatus(bytes);
        Double[] values = new Double[]{batteryStatus};
        return values;
    }

    @Override
    public int getPacketMsgID() {
        // TODO Auto-generated method stub
        return GP_MSG_ID;
    }

}
