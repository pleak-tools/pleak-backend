package com.naples.json;

import com.naples.user.User;

public class JsonUser implements Comparable<JsonUser>, java.io.Serializable {

  // Database
  Integer id;
  String email;

  public JsonUser() {}

  public JsonUser(User user) {
    this.id = user.getId();
    this.email = user.getEmail();
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public int compareTo(JsonUser user) {
      return this.id-user.getId();
  }

}
