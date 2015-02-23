package org.gisandchips.ctmdroid.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by benizar on 23/02/2015.
 */
public class Station extends RealmObject {
    @PrimaryKey
    private int stationId=0;
    private String stationName="";
    private RealmList<Sample> sampleList;

    private double latitude=0.0;
    private double longitude=0.0;
    private double altitude=0.0;

    // Standard getters & setters
    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public RealmList<Sample> getSampleList() {
        return sampleList;
    }

    public void setSampleList(RealmList<Sample> sampleList) {
        this.sampleList = sampleList;
    }

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
}
