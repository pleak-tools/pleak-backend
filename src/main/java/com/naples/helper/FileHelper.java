package com.naples.helper;

import javax.servlet.http.Part;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
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
    List<String> fileModifiedDates = new ArrayList<>();

    for (Path entry: filePaths) {
      fileModifiedDates.add(getFileLastModifiedString(entry));
    }

    return fileModifiedDates;
  }

  public void saveFile(String fileContent, String fileMD5, String filePathStr) throws FileException, NoSuchAlgorithmException, IOException {
    InputStream content = new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));

    try {
      String curFileMD5 = getMD5Hash(filePathStr);
      if (!curFileMD5.equals(fileMD5)) throw new FileException("File content changed.", 409);
    } catch (FileNotFoundException ex) {
      // File doesn't exist but it's okay
    }

    Path filePath = Paths.get(filePathStr);
    Files.copy(content, filePath, StandardCopyOption.REPLACE_EXISTING);
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

  public String getFileLastModifiedString(Path filePath) {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    return sdf.format(filePath.toFile().lastModified()).toString();
  }

  public String getContent(Path filePath) throws IOException {
    return new String(Files.readAllBytes(filePath));
  }

  // http://www.mkyong.com/java/java-md5-hashing-example/
  public String getMD5Hash(String filePathStr) throws NoSuchAlgorithmException, IOException {
    MessageDigest md = MessageDigest.getInstance("MD5");
    FileInputStream fis = new FileInputStream(filePathStr);

    byte[] dataBytes = new byte[1024];

    int nread = 0;
    while ((nread = fis.read(dataBytes)) != -1) {
      md.update(dataBytes, 0, nread);
    };
    byte[] mdbytes = md.digest();

    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < mdbytes.length; i++) {
      sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
    }

    return sb.toString();
  }

}