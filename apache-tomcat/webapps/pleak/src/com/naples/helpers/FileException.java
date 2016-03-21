package com.naples.helpers;

public class FileException extends Exception {
  protected int code;

  public FileException() {}

  public FileException(String message)
  {
    super(message);
  }

  public FileException(String message, int code)
  {
    super(message);
    this.code = code;
  }

  public int getCode() {
    return code;
  }
}