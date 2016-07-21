package com.naples.file;

import com.naples.user.User;
import com.naples.helper.Action;

public class Permission {

    Integer id;
    Pobject pobject;
    User user;
    Action action;

    public Permission() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Pobject getPobject() {
        return pobject;
    }

    public void setPobject(Pobject pobject) {
        this.pobject = pobject;
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
