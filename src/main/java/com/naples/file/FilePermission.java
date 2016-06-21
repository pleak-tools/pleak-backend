package com.naples.file;

import com.naples.user.User;
import com.naples.helper.Action;

public class FilePermission {

    Integer id;
    File file;
    User user;
    Action action;

    public FilePermission() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

}
