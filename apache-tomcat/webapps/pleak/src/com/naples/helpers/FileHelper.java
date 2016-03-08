package com.naples.helpers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileHelper {

  public boolean isCorrectFileName(String fileName) {
    String WHITELIST = "[^0-9A-Za-z.]+";

    Pattern p = Pattern.compile(WHITELIST);
    Matcher m = p.matcher(fileName);

    return !m.find();
  }

  public boolean isCorrectFileExtension(String fileName) {
    String CORRECT_FILE_EXTENSION = "\\.bpmn$";

    Pattern p = Pattern.compile(CORRECT_FILE_EXTENSION);
    Matcher m = p.matcher(fileName);

    return m.find();
  }

}