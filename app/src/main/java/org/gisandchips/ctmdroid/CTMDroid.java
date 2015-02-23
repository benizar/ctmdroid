package org.gisandchips.ctmdroid;

import android.app.Application;

import org.gisandchips.ctmdroid.model.Project;
import org.gisandchips.ctmdroid.model.ProjectManager;

/**
 * Created by benizar on 23/02/2015.
 */
public class CTMDroid extends Application {

    //List all CTM projects
    public static ProjectManager currentProjectManager = new ProjectManager();

    //Current project
    public static Project currentProject = new Project();


    private static CTMDroid singleton;

    public static CTMDroid getInstance() {
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
    }
}
