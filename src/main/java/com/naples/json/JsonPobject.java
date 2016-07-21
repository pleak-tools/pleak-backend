package com.naples.json;

import java.util.HashSet;
import java.util.Set;

import com.naples.file.Pobject;
import com.naples.file.Permission;

public class JsonPobject implements Comparable<JsonPobject>, java.io.Serializable {

    // Database
    Integer id;
    String title;
    JsonUser user;
    Set<JsonPermission> permissions = new HashSet<JsonPermission>(0);
    String type;
    JsonDirectory directory;

    public JsonPobject() {}

    public JsonPobject(Pobject pobject) {
        this.id = pobject.getId();
        this.title = pobject.getTitle();
        this.user = new JsonUser(pobject.getUser());
        this.directory = new JsonDirectory();
        this.directory.setId(pobject.getDirectory().getId());
        for (Permission p : pobject.getPermissions()) {
            this.permissions.add(new JsonPermission(p));
        }
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public JsonUser getUser() {
        return user;
    }
    public void setUser(JsonUser user) {
        this.user = user;
    }

    public Set<JsonPermission> getPermissions() {
        return permissions;
    }
    public void setPermissions(Set<JsonPermission> permissions) {
        this.permissions = permissions;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public JsonDirectory getDirectory() {
        return directory;
    }
    public void setDirectory(JsonDirectory directory) {
        this.directory = directory;
    }

    @Override
    public int compareTo(JsonPobject pobject) {
        return this.id-pobject.getId();
    }

}
