package com.naples.json;

import com.naples.file.Permission;
import com.naples.helper.Action;

public class JsonPermission implements Comparable<JsonPermission>, java.io.Serializable {

    Integer id;
    Action action;
    JsonUser user;

    public JsonPermission() {}

    public JsonPermission(Permission p) {
        this.id = p.getId();
        this.action = p.getAction();
        this.user = new JsonUser(p.getUser());
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
    public int compareTo(JsonPermission permission) {
        return this.user.getId()-permission.user.getId();
    }
}
