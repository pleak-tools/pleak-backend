package com.naples.responses;

import com.naples.responses.ResponseData;

public class ResponseDataText extends ResponseData {

  public String text;

  protected String getText() {
    return text;
  }

  protected void setText(String text) {
    this.text = text;
  }

}