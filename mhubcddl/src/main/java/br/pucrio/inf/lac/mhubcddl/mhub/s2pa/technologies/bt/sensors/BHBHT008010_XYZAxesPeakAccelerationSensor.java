/**
 *
 */
package br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors;

import java.util.UUID;

/**
 * @author bertodetacio
 */
public class BHBHT008010_XYZAxesPeakAccelerationSensor extends BHBHT008010_AbstractSensor {


    private static BHBHT008010_XYZAxesPeakAccelerationSensor instance;

    /**
     *
     */
    public BHBHT008010_XYZAxesPeakAccelerationSensor() {
        // TODO Auto-generated constructor stub
    }

    public static BHBHT008010_XYZAxesPeakAccelerationSensor getInstance() {
        if (instance == null) {
            instance = new BHBHT008010_XYZAxesPeakAccelerationSensor();
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
        double X_AxisPeakAcceleration = getGeneralPacketInfo().GetX_AxisAccnPeak(bytes);
        double Y_AxisPeakAcceleration = getGeneralPacketInfo().GetY_AxisAccnPeak(bytes);
        double Z_AxisPeakAcceleration = getGeneralPacketInfo().GetZ_AxisAccnPeak(bytes);

        Double[] values = new Double[]{X_AxisPeakAcceleration, Y_AxisPeakAcceleration, Z_AxisPeakAcceleration};
        return values;
    }

    @Override
    public int getPacketMsgID() {
        // TODO Auto-generated method stub
        return GP_MSG_ID;
    }

}
