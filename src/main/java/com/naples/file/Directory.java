package com.naples.file;

import org.hibernate.Session;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Directory extends Pobject {

    Set<Pobject> pobjects = new HashSet<Pobject>(0);

    public Directory() {}

    public Set<Pobject> getPobjects() {
        return pobjects;
    }
    public void setPobjects(Set<Pobject> pobjects) {
        this.pobjects = pobjects;
    }

    public void build(ServletContext context) {
        for (Pobject po : pobjects) {
            if (po instanceof Directory) {
                ((Directory)po).build(context);
            } else if (po instanceof File) {
                ((File)po).build(context);
            }
        }
    }

    public void loadLastModified() {
        for (Pobject po : pobjects) {
            if (po instanceof Directory) {
                ((Directory)po).loadLastModified();
            } else if (po instanceof File) {
                ((File)po).loadLastModified();
            }
        }
    }

    public void delete() throws FileException, IOException {
        for (Pobject po : pobjects) {
            if (po instanceof Directory) {
                ((Directory)po).delete();
            } else if (po instanceof File) {
                ((File)po).delete();
            }
        }
    }

    @Override
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
        for (Pobject pob : pobjects) {
            pob.inheritPermissions(session);
        }
    }

    public boolean canBeViewedBy(int userId) {
        if (this.user.getId() == userId) {
            return true;
        } else {
            Iterator iterator = permissions.iterator();
            while (iterator.hasNext()) {
                Permission p = (Permission)iterator.next();
                if (p.getUser().getId() == userId &&
                    (p.getAction().getTitle().equals("view") ||
                     p.getAction().getTitle().equals("edit")) ) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean canBeEditedBy(int userId) {
        if (this.user.getId() == userId) {
            return true;
        } else {
            Iterator iterator = permissions.iterator();
            while (iterator.hasNext()) {
                Permission p = (Permission)iterator.next();
                if (p.getUser().getId() == userId && p.getAction().getTitle().equals("edit") ) {
                    return true;
                }
            }
        }
        return false;
    }

}
