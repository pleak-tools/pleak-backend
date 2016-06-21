package com.naples.json;

import com.naples.file.FilePermission;
import com.naples.helper.Action;

public class JsonFilePermission implements Comparable<JsonFilePermission>, java.io.Serializable {

    Integer id;
    Action action;
    JsonUser user;

    public JsonFilePermission() {}

    public JsonFilePermission(FilePermission fp) {
        this.id = fp.getId();
        this.action = fp.getAction();
        this.user = new JsonUser(fp.getUser());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public JsonUser getUser() {
        return user;
    }

    public void setUser(JsonUser user) {
        this.user = user;
    }

    @Override
    public int compareTo(JsonFilePermission filePermission) {
        return this.user.getId()-filePermission.user.getId();
    }
}
