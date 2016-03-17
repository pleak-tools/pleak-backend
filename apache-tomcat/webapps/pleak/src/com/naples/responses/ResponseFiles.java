package com.naples.responses;

import java.util.ArrayList;
import java.util.List;

import com.naples.responses.ResponseBase;

public class ResponseFiles extends ResponseBase {
  public List<String> fileNames = new ArrayList<>();
  public List<String> fileModifiedDates = new ArrayList<>();

  protected void setFileNames(List<String> fileNames) {
    this.fileNames = fileNames;
  }

  protected List<String> getFileNames() {
    return fileNames;
  }

  protected void setFileModifiedDates(List<String> fileModifiedDates) {
    this.fileModifiedDates = fileModifiedDates;
  }

  protected List<String> getFileModifiedDates() {
    return fileModifiedDates;
  }
}