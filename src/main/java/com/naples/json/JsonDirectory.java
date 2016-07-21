package com.naples.json;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import com.naples.file.Pobject;
import com.naples.file.File;
import com.naples.file.Directory;
import com.naples.file.Permission;

public class JsonDirectory extends JsonPobject implements java.io.Serializable {

    List<JsonPobject> pobjects = new ArrayList<JsonPobject>(0);

    public JsonDirectory() {}

    public JsonDirectory(Directory directory) {
        this.type = "directory";
        this.id = directory.getId();
        this.title = directory.getTitle();
        this.user = new JsonUser(directory.getUser());
        this.directory = new JsonDirectory();
        if (directory.getDirectory() != null) this.directory.setId(directory.getDirectory().getId());
        for (Pobject po : directory.getPobjects()) {
            if (po instanceof Directory) {
                this.pobjects.add(new JsonDirectory((Directory)po));
            } else if (po instanceof File) {
                this.pobjects.add(new JsonFile((File)po));
            }
        }
        for (Permission p : directory.getPermissions()) {
            this.permissions.add(new JsonPermission(p));
        }
        Collections.sort(this.pobjects);
    }

    public List<JsonPobject> getPobjects() {
        return pobjects;
    }
    public void setPobjects(List<JsonPobject> pobjects) {
        this.pobjects = pobjects;
    }

}
