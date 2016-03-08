package com.naples.responses;

import java.util.ArrayList;
import java.util.List;

import com.naples.responses.ResponseData;

public class ResponseDataList extends ResponseData {
  public List<String> list = new ArrayList<>();

  protected void setList(List<String> list) {
    this.list = list;
  }

  protected List<String> getList() {
    return list;
  }
}