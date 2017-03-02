package com.naples.user;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserHelper {

  public boolean isValidPassword(String password) {
    String LETTERS = "^((?=.*[a-z])|(?=.*[A-Z]))";
    String NUMBERS = "(?=.*[0-9])";
    String LENGTH = "(?=.{8,})";

    Pattern pattern = Pattern.compile(LETTERS + NUMBERS + LENGTH);

    Matcher matcher = pattern.matcher(password);

    return matcher.find();
  }

}
