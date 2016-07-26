package com.naples.file;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Iterator;

import org.hibernate.Session;
import org.hibernate.Filter;

import com.naples.util.HibernateUtil;
import com.naples.user.User;
import com.naples.helper.Action;
import com.naples.json.JsonPobject;
import com.naples.json.JsonPermission;

// Pleak Object
public class Pobject implements Comparable<Pobject> {

    // Database
    Integer id;
    String title;
    User user;
    Pobject directory;
    Set<Permission> permissions = new HashSet<Permission>(0);

    public Pobject() {}

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

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }
    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public Pobject getDirectory() {
        return directory;
    }
    public void setDirectory(Pobject directory) {
        this.directory = directory;
    }

    public boolean updatePermissions(JsonPobject pobject, Session session) {
        boolean changesMade = false;

        Iterator<Permission> iter = permissions.iterator();
        while (iter.hasNext()) {
            Permission p = iter.next();

            boolean match = false;
            for (JsonPermission jp : pobject.getPermissions()) {
                boolean userMatch = jp.getUser().getId() == p.getUser().getId();
                boolean actionMatch = jp.getAction().getTitle().equals(p.getAction().getTitle());
                if (userMatch && actionMatch) {
                    match = true;
                    break;
                }
            }

            if (!match) {
                iter.remove();
                session.delete(p);
                session.flush();

                if (!changesMade) changesMade = true;
            }
        }

        // Add new permissions
        for (JsonPermission jp : pobject.getPermissions()) {
            boolean match = false;
            for (Permission p : permissions) {
                boolean userMatch = jp.getUser().getId() == p.getUser().getId();
                boolean actionMatch = jp.getAction().getTitle().equals(p.getAction().getTitle());
                if (userMatch && actionMatch) {
                    match = true;
                    break;
                }
            }

            if (!match) {
                Permission newPermission = new Permission();
                newPermission.setPobject(this);

                Filter filter = session.enableFilter("userFilterByEmail");
                filter.setParameter("userFilterParam", jp.getUser().getEmail());
                List<User> users = (List<User>) session.createCriteria(User.class).list();
                User dbUser = (User) users.get(0);
                session.disableFilter("userFilterByEmail");
                newPermission.setUser(dbUser);

                filter = session.enableFilter("actionFilterByTitle");
                filter.setParameter("actionFilterParam", jp.getAction().getTitle());
                List<Action> actions = (List<Action>) session.createCriteria(Action.class).list();
                Action dbAction = (Action) actions.get(0);
                session.disableFilter("actionFilterByTitle");
                newPermission.setAction(dbAction);

                permissions.add(newPermission);
                session.save(newPermission);

                if (!changesMade) changesMade = true;
            }
        }

        return changesMade;
    }

    public void inheritPermissions(Session session) {
        session.save(this);
        for (Permission p : permissions) {
            session.delete(p);
        }
        permissions.clear();
        session.flush();

        if (directory == null) {
            return;
        }
        for (Permission p : directory.getPermissions()) {
            Permission np = new Permission();
            np.setUser(p.getUser());
            np.setAction(p.getAction());
            np.setPobject(this);
            session.save(np);
            permissions.add(np);
        }
    }

    @Override
    public int compareTo(Pobject pobject) {
        return this.id-pobject.getId();
    }

}
