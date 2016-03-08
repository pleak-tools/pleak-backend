package com.naples.responses;

import com.naples.responses.ResponseBase;

public class ResponseText extends ResponseBase {

  public String text;

  protected String getText() {
    return text;
  }

  protected void setText(String text) {
    this.text = text;
  }

}