package com.naples.helpers;

import javax.servlet.http.Part;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.nio.file.*;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;

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

  public List<Path> getFilePaths(Path filesPath) throws IOException {
    List<Path> filePaths = new ArrayList<>();

    DirectoryStream<Path> stream = Files.newDirectoryStream(filesPath, "*.bpmn");

    for (Path entry: stream) {
      filePaths.add(entry);
    }
    Collections.sort(filePaths);

    return filePaths;
  }

  public List<String> getFileNames(List<Path> filePaths) throws IOException {
    List<String> fileNames = new ArrayList<>();

    for (Path entry: filePaths) {
      fileNames.add(entry.getFileName().toString());
    }

    return fileNames;
  }

  public List<String> getFileModifiedDates(List<Path> filePaths) throws IOException {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    List<String> fileModifiedDates = new ArrayList<>();

    for (Path entry: filePaths) {
      fileModifiedDates.add(sdf.format(entry.toFile().lastModified()).toString());
    }

    return fileModifiedDates;
  }

  public void saveFile(Part filePart, String filePathStr) throws IOException {
    InputStream fileContent = filePart.getInputStream();

    Path filePath = Paths.get(filePathStr);

    Files.copy(fileContent, filePath, StandardCopyOption.REPLACE_EXISTING);
  }

  public boolean deleteFile(String filePathStr) throws IOException, FileException {
    if (!isExistingFile(filePathStr)) return false;

    Path filePath = Paths.get(filePathStr);
    Files.delete(filePath);

    return true;
  }

  public boolean isExistingFile(String filePathStr) {
    File f = new File(filePathStr);
    if (f.exists() && !f.isDirectory()) {
      return true;
    }

    return false;
  }

}