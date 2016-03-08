package com.naples.responses;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.naples.responses.ResponseData;

public class ResponseBase {

  public String type;
  public ResponseData data;

  protected void setType(String type) {
    this.type = type;
  }

  protected void setData(ResponseData data) {
    this.data = data;
  }

  protected String getType() {
    return type;
  }

  protected ResponseData getData() {
    return data;
  }

}