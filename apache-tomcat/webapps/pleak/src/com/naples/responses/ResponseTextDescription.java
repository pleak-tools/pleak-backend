package com.naples.responses;

import com.naples.responses.ResponseText;

public class ResponseTextDescription extends ResponseText {

  public String description;

  protected String getDescription() {
    return description;
  }

  protected void setDescription(String description) {
    this.description = description;
  }

}