package org.gisandchips.ctmdroid.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by benizar on 23/02/2015.
 */
public class Project extends RealmObject {
    @PrimaryKey
    private int projectId=0;
    private String projectName="";
    private RealmList<Station> stationList;

    // Standard getters & setters
    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public RealmList<Station> getStationList() {
        return stationList;
    }

    public void setStationList(RealmList<Station> stationList) {
        this.stationList = stationList;
    }
}
