package com.naples.file;

import java.util.List;
import java.util.ArrayList;

import com.naples.file.File;

public class Files implements java.io.Serializable {

    List<File> files;

    public Files() {
        files = new ArrayList<>();
    }

    public Files(List<File> files) {
        this.files = files;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }
}