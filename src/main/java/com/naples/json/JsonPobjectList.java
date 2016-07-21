package com.naples.json;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import com.naples.file.Pobject;
import com.naples.file.File;
import com.naples.file.Directory;

public class JsonPobjectList implements java.io.Serializable {

    List<JsonPobject> pobjects = new ArrayList<>();

    public JsonPobjectList() {}

    public JsonPobjectList(Set<Pobject> pobjects) {
        for (Pobject po : pobjects) {
            if (po instanceof Directory) {
                this.pobjects.add(new JsonDirectory((Directory)po));
            } else if (po instanceof File) {
                this.pobjects.add(new JsonFile((File)po));
            }
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
