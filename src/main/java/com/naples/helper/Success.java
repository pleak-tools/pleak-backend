package com.naples.helper;

public class Success implements java.io.Serializable {

  public String success;
  public int id;

  public Success(String success) {
    this.success = success;
  }

  public Success(String success, int id) {
    this.success = success;
    this.id = id;
  }
}
