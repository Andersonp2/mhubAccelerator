package br.pucrio.inf.lac.mhubcddl.mhub.models.locals;

import com.google.gson.Gson;

/**
 * Created by lcmuniz on 11/02/17.
 */

public class SourceLocation {

    private double latitude;
    private double longitude;
    private double altitude;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
