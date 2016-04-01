package com.naples.file;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class FileList implements java.io.Serializable {

    List<File> files = new ArrayList<>();

    public FileList() {}

    public FileList(Set<File> files) {
        this.files.addAll(files);
        Collections.sort(this.files);
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }
}