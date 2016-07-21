package com.naples.user;

import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

import com.naples.file.Pobject;
import com.naples.file.Permission;
import com.naples.file.Directory;
import com.naples.bcrypt.BCrypt;

public class User {

  // Database
  Integer id;
  String email;
  String password;
  Set<Pobject> pobjects = new HashSet<Pobject>(0);
  Set<Permission> permissions = new HashSet<Permission>(0);

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

  public Set<Pobject> getPobjects() {
    return pobjects;
  }
  public void setPobjects(Set<Pobject> pobjects) {
    this.pobjects = pobjects;
  }

  public Set<Permission> getPermissions() {
    return permissions;
  }
  public void setPermissions(Set<Permission> permissions) {
    this.permissions = permissions;
  }

  public Directory getRoot() {
    for (Pobject po : pobjects) {
      if (po.getDirectory() == null && po.getTitle().equals("root")) {
        return (Directory) po;
      }
    }
    return new Directory();
  }

  public Directory getShared() {
    Directory shared = new Directory();
    shared.setTitle("shared");
    shared.setUser(this);

    for (Permission p : permissions) {
      Pobject pobjectParent = p.getPobject().getDirectory();
      if (pobjectParent != null) {
        boolean hasRightsToParent = false;
        for (Permission pp : pobjectParent.getPermissions()) {
          if (pp.getUser() == this) {
            hasRightsToParent = true;
            break;
          }
        }
        if (!hasRightsToParent) {
          shared.getPobjects().add(p.getPobject());
        }
      } else {
        shared.getPobjects().add(p.getPobject());
      }
    }

    return shared;
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

  public boolean canView(Pobject pobject) {
    boolean canView = false;
    // TODO: might be faster with a query at some point?
    Iterator iterator = permissions.iterator();
    while (iterator.hasNext()) {
      Permission p = (Permission)iterator.next();
      if (p.getPobject() == pobject && p.getAction().getTitle().equals("view")) {
        canView = true;
      }
    }
    return canView;
  }

  public boolean canEdit(Pobject pobject) {
    boolean canEdit = false;
    // TODO: might be faster with a query at some point?
    Iterator iterator = permissions.iterator();
    while (iterator.hasNext()) {
      Permission p = (Permission)iterator.next();
      if (p.getPobject() == pobject && p.getAction().getTitle().equals("edit")) {
        canEdit = true;
      }
    }
    return canEdit;
  }

  public Set<Pobject> getAllPobjects() {
    Set<Pobject> allPobjects = new HashSet<Pobject>(0);
    allPobjects.addAll(pobjects);

    Iterator iterator = permissions.iterator();
    while (iterator.hasNext()) {
      Permission p = (Permission)iterator.next();
      if ( !allPobjects.contains(p.getPobject()) ) {
        allPobjects.add(p.getPobject());
      }
    }

    return allPobjects;
  }

  @Override
  public String toString() {
    return "User: [ " + email + " | " + password + " ]";
  }

}
