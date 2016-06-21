package com.naples.json;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import com.naples.file.File;

public class JsonFileList implements java.io.Serializable {

    List<JsonFile> files = new ArrayList<>();

    public JsonFileList() {}

    public JsonFileList(Set<File> files) {
        for (File file : files) {
            this.files.add(new JsonFile(file));
        }
        Collections.sort(this.files);
    }

    public List<JsonFile> getFiles() {
        return files;
    }

    public void setFiles(List<JsonFile> files) {
        this.files = files;
    }
}
