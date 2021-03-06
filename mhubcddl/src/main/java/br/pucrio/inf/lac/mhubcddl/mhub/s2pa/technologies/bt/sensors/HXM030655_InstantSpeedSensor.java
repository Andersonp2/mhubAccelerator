package br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt.sensors;


import java.util.UUID;

public class HXM030655_InstantSpeedSensor extends HXM030655_AbstractSensor {

    private static HXM030655_InstantSpeedSensor instance;

    private HXM030655_InstantSpeedSensor() {
        // TODO Auto-generated constructor stub
    }

    public static HXM030655_InstantSpeedSensor getInstance() {
        if (instance == null) {
            instance = new HXM030655_InstantSpeedSensor();
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
        double instanteSpeed = getHRSpeedDistPacketInfo().GetInstantSpeed(bytes);
        return new Double[]{instanteSpeed};
    }

    @Override
    public int getPacketMsgID() {
        // TODO Auto-generated method stub
        return HR_SPD_DIST_PACKET;
    }


}
