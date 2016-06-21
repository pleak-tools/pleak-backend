package com.naples.user;

import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

import com.naples.file.File;
import com.naples.file.FilePermission;
import com.naples.bcrypt.BCrypt;

public class User implements java.io.Serializable {

  // Database
  Integer id;
  String email;
  String password;
  Set<File> files = new HashSet<File>(0);
  Set<FilePermission> filePermissions = new HashSet<FilePermission>(0);

  public User() {}

  public User(Integer id, String email, String password) {
    this.id = id;
    this.email = email;
    this.password = password;
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

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Set<File> getFiles() {
    return files;
  }

  public void setFiles(Set<File> files) {
    this.files = files;
  }

  public Set<FilePermission> getFilePermissions() {
    return filePermissions;
  }

  public void setFilePermissions(Set<FilePermission> filePermissions) {
    this.filePermissions = filePermissions;
  }

  public boolean isCorrectPassword(String plaintext) {
    return BCrypt.checkpw(plaintext, this.password);
  }

  public void createHashedPassword(String plaintext) {
    this.password = BCrypt.hashpw(plaintext, BCrypt.gensalt(12));
  }

  public boolean changePassword(String oldPassword, String newPassword) {
    if ( isCorrectPassword(oldPassword) ) {
      createHashedPassword(newPassword);
      return true;
    }
    return false;
  }

  public boolean canView(File file) {
    boolean canView = false;
    // TODO: might be faster with a query at some point?
    Iterator iterator = filePermissions.iterator();
    while (iterator.hasNext()) {
      FilePermission fp = (FilePermission)iterator.next();
      if (fp.getFile() == file && fp.getAction().getTitle().equals("view")) {
        canView = true;
      }
    }
    return canView;
  }

  public boolean canEdit(File file) {
    boolean canView = false;
    // TODO: might be faster with a query at some point?
    Iterator iterator = filePermissions.iterator();
    while (iterator.hasNext()) {
      FilePermission fp = (FilePermission)iterator.next();
      if (fp.getFile() == file && fp.getAction().getTitle().equals("edit")) {
        canView = true;
      }
    }
    return canView;
  }

  public Set<File> getAllFiles() {
    Set<File> allFiles = new HashSet<File>(0);
    allFiles.addAll(files);

    Iterator iterator = filePermissions.iterator();
    while (iterator.hasNext()) {
      FilePermission fp = (FilePermission)iterator.next();
      if ( !allFiles.contains(fp.getFile()) ) {
        allFiles.add(fp.getFile());
      }
    }

    return allFiles;
  }

  @Override
  public String toString() {
    return "User: [ " + email + " | " + password + " ]";
  }

}
