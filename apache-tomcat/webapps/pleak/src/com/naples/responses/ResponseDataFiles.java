package com.naples.responses;

import java.util.ArrayList;
import java.util.List;

import com.naples.responses.ResponseData;

public class ResponseDataFiles extends ResponseData {
    public List<String> files = new ArrayList<>();

    protected void setFiles(List<String> files) {
        this.files = files;
    }

    protected List<String> getFiles() {
        return files;
    }
}