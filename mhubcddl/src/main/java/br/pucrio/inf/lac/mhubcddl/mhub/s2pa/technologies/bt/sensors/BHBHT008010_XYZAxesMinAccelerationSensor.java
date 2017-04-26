/**
 *
 */
package br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors;

import java.util.UUID;

/**
 * @author bertodetacio
 */
public class BHBHT008010_XYZAxesMinAccelerationSensor extends BHBHT008010_AbstractSensor {


    private static BHBHT008010_XYZAxesMinAccelerationSensor instance;

    /**
     *
     */
    public BHBHT008010_XYZAxesMinAccelerationSensor() {
        // TODO Auto-generated constructor stub
    }

    public static BHBHT008010_XYZAxesMinAccelerationSensor getInstance() {
        if (instance == null) {
            instance = new BHBHT008010_XYZAxesMinAccelerationSensor();
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
        double X_AxisMinAcceleration = getGeneralPacketInfo().GetX_AxisAccnPeak(bytes);
        double Y_AxisMincceleration = getGeneralPacketInfo().GetY_AxisAccnPeak(bytes);
        double Z_AxisMinAcceleration = getGeneralPacketInfo().GetZ_AxisAccnPeak(bytes);

        Double[] values = new Double[]{X_AxisMinAcceleration, Y_AxisMincceleration, Z_AxisMinAcceleration};
        return values;
    }

    @Override
    public int getPacketMsgID() {
        // TODO Auto-generated method stub
        return GP_MSG_ID;
    }

}
