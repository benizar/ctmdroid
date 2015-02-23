package org.gisandchips.ctmdroid.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by benizar on 23/02/2015.
 */
public class ProjectManager extends RealmObject {
    @PrimaryKey
    private RealmList<Project> projectsList;

    // Standard getters & setters
    public void setProjects(RealmList<Project> projects) {
        this.projectsList = projects;
    }

    public RealmList<Project> getProjects() {
        return projectsList;
    }
}
