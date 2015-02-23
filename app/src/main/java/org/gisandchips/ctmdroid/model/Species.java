package org.gisandchips.ctmdroid.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by benizar on 23/02/2015.
 */
public class Species extends RealmObject {
    private String speciesId="";// For abundance studies
    private String speciesName="";
    private int speciesCount=0;

    // Standard getters & setters
    public String getSpeciesId() {
        return speciesId;
    }

    public void setSpeciesId(String speciesId) {
        this.speciesId = speciesId;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public void setSpeciesName(String speciesName) {
        this.speciesName = speciesName;
    }

    public int getSpeciesCount() {
        return speciesCount;
    }

    public void setSpeciesCount(int speciesCount) {
        this.speciesCount = speciesCount;
    }
}
