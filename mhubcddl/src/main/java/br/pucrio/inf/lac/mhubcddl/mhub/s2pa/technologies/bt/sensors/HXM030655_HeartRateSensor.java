package br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors;


import java.util.UUID;

public class HXM030655_HeartRateSensor extends HXM030655_AbstractSensor {

    private static HXM030655_HeartRateSensor instance;

    private HXM030655_HeartRateSensor() {
        super();
    }

    public static HXM030655_HeartRateSensor getInstance() {
        if (instance == null) {
            instance = new HXM030655_HeartRateSensor();
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
    public synchronized Double[] convert(byte[] bytes) {
        // TODO Auto-generated method stub
        double heartRate = getHRSpeedDistPacketInfo().GetHeartRate(bytes);
        return new Double[]{heartRate};
    }

    @Override
    public int getPacketMsgID() {
        // TODO Auto-generated method stub
        return HR_SPD_DIST_PACKET;
    }


}
