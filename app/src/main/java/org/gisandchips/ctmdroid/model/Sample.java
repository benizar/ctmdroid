package org.gisandchips.ctmdroid.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by benizar on 23/02/2015.
 */
public class Sample extends RealmObject {
    @PrimaryKey
    private int sampleId=0;
    private String sampleName="";
    private boolean isManagement=false;
    private RealmList<Species> speciesRealmList;

    // Standard getters & setters
    public int getSampleId() {
        return sampleId;
    }

    public void setSampleId(int sampleId) {
        this.sampleId = sampleId;
    }

    public String getSampleName() {
        return sampleName;
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    public boolean isManagement() {
        return isManagement;
    }

    public void setManagement(boolean isManagement) {
        this.isManagement = isManagement;
    }

    public RealmList<Species> getSpeciesRealmList() {
        return speciesRealmList;
    }

    public void setSpeciesRealmList(RealmList<Species> speciesRealmList) {
        this.speciesRealmList = speciesRealmList;
    }
}
