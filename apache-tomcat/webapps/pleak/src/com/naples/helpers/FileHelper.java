package com.naples.helpers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileHelper {

  public boolean isCorrectFileName(String fileName) {
    String WHITELIST = "[^0-9A-Za-z.\\-_]+";
    String BEGINNING = "^[^\\.]";

    Pattern pw = Pattern.compile(WHITELIST);
    Pattern pb = Pattern.compile(BEGINNING);
    Matcher mw = pw.matcher(fileName);
    Matcher mb = pb.matcher(fileName);

    return !mw.find() && mb.find();
  }

  public boolean isCorrectFileExtension(String fileName) {
    String CORRECT_FILE_EXTENSION = "\\.bpmn$";

    Pattern p = Pattern.compile(CORRECT_FILE_EXTENSION);
    Matcher m = p.matcher(fileName);

    return m.find();
  }

}