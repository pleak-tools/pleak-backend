package com.naples.responses;

public class ResponseBase {

  public String type;
  public Integer code;

  protected String getType() {
    return type;
  }

  protected void setType(String type) {
    this.type = type;
  }

  protected Integer getCode() {
    return code;
  }

  protected void setCode(Integer code) {
    this.code = code;
  }

}