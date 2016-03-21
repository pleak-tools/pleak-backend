package com.naples.responses;

import com.naples.responses.ResponseBase;

public class ResponseFile extends ResponseBase {

  public String content;
  public String lastModified;
  public String md5;

  protected String getContent() {
    return content;
  }

  protected void setContent(String content) {
    this.content = content;
  }

  protected String getLastModified() {
    return lastModified;
  }

  protected void setLastModified(String lastModified) {
    this.lastModified = lastModified;
  }

  protected String getMD5() {
    return md5;
  }

  protected void setMD5(String md5) {
    this.md5 = md5;
  }

}