package br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors;


import java.util.UUID;

public class HXM030655_DistanceSensor extends HXM030655_AbstractSensor {

    private static HXM030655_DistanceSensor instance;

    private HXM030655_DistanceSensor() {
        // TODO Auto-generated constructor stub
    }

    public static HXM030655_DistanceSensor getInstance() {
        if (instance == null) {
            instance = new HXM030655_DistanceSensor();
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
        double distance = getHRSpeedDistPacketInfo().GetDistance(bytes);
        return new Double[]{distance};
    }

    @Override
    public int getPacketMsgID() {
        // TODO Auto-generated method stub
        return HR_SPD_DIST_PACKET;
    }


}
