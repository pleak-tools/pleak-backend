package com.naples.rest;

//import java.util.Set;
//import java.util.HashSet;
import java.util.Date;

import java.io.FileNotFoundException;

import javax.servlet.ServletContext;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import javax.ws.rs.container.ContainerRequestContext;

import javax.annotation.security.PermitAll;

import org.hibernate.Session;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import com.naples.util.HibernateUtil;

import com.naples.user.User;

import com.naples.generator.RandomStringGenerator;

import com.naples.file.Pobject;
import com.naples.file.File;
import com.naples.file.FileHelper;
import com.naples.file.FileException;
import com.naples.file.Directory;

import com.naples.helper.Error;

import com.naples.json.JsonFile;
import com.naples.json.JsonDirectory;
//import com.naples.json.JsonPobject;
//import com.naples.json.JsonPobjectList;

@Path("/directories")
public class FileService {

    @Context
    private ServletContext context;

    /*
    @GET
    public Response getAll(@Context ContainerRequestContext crc) {
        int userId = (int) crc.getProperty("userId");
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        try {
            User user = (User) session.get(User.class, userId);

            Directory root = user.getRoot();
            root.build(context);

            //Directory share = user.getShare();
            //share.build(context);

            Set<Pobject> pobjects = new HashSet<Pobject>(0);
            pobjects.add(root);
            //pobjects.add(share);

            return Response.ok(new JsonPobjectList(pobjects), MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            //e.printStackTrace();
            return Response.status(400).entity("Server error.").build();
        } finally {
            session.close();
        }
    }*/

    @POST
    public Response create(JsonDirectory dir, @Context ContainerRequestContext crc) {
        int userId = (int) crc.getProperty("userId");
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        FileHelper fh = new FileHelper();

        try {
            if ( !fh.isCorrectPobjectName(dir.getTitle()) ) throw new FileException("Incorrect directory name.");

            User user = (User) session.get(User.class, userId);

            Directory parent = null;
            if (dir.getDirectory().getTitle() != null && dir.getDirectory().getTitle().equals("root")) {
                parent = user.getRoot();
            } else if (dir.getDirectory().getId() != null) {
                parent = (Directory) session.get(Directory.class, dir.getDirectory().getId());
            }

            Directory newDir = null;
            if (parent == null) {
                return Response.status(404).entity(new Error("Parent directory not found.")).type(MediaType.APPLICATION_JSON).build();
            } else if (parent.getUser().getId() != userId) {
                return Response.status(403).entity(new Error("Forbidden.")).type(MediaType.APPLICATION_JSON).build();
            } else {
                newDir = new Directory();
                newDir.setTitle(dir.getTitle());
                newDir.setUser(user);
                newDir.setDirectory(parent);
                if (user.getRoot() != parent) {
                    newDir.inheritPermissions(session);
                }
            }

            session.save(newDir);
            session.getTransaction().commit();

            return Response.status(200).entity(new JsonDirectory(newDir)).type(MediaType.APPLICATION_JSON).build();
        } catch(FileException e) {
            //e.printStackTrace();
            return Response.status(e.getCode()).entity(new Error(e.getMessage())).type(MediaType.APPLICATION_JSON).build();
        } catch(Exception e) {
            //e.printStackTrace();
            return Response.status(400).entity(new Error("Server error.")).type(MediaType.APPLICATION_JSON).build();
        } finally {
            session.close();
        }
    }

    @GET
    @Path("/root")
    public Response getRoot(@Context ContainerRequestContext crc) {
        int userId = (int) crc.getProperty("userId");
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        try {
            User user = (User) session.get(User.class, userId);

            Directory root = user.getRoot();
            root.build(context);

            return Response.ok(new JsonDirectory(root), MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            //e.printStackTrace();
            return Response.status(400).entity("Server error.").build();
        } finally {
            session.close();
        }
    }

    @GET
    @Path("/shared")
    public Response getShared(@Context ContainerRequestContext crc) {
        int userId = (int) crc.getProperty("userId");
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        try {
            User user = (User) session.get(User.class, userId);

            Directory shared = user.getShared();
            shared.build(context);

            return Response.ok(new JsonDirectory(shared), MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            //e.printStackTrace();
            return Response.status(400).entity("Server error.").build();
        } finally {
            session.close();
        }
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") int id, @Context ContainerRequestContext crc) {
        int userId = (int) crc.getProperty("userId");
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        try {
            User user = (User) session.get(User.class, userId);
            Directory dir = (Directory) session.get(Directory.class, id);

            if (dir == null) {
                return Response.status(404).entity(new Error("Directory not found.")).type(MediaType.APPLICATION_JSON).build();
            } else if (dir.getUser() != user) {
                return Response.status(403).entity(new Error("Forbidden.")).type(MediaType.APPLICATION_JSON).build();
            }
            //TODO: add shared directories permission

            dir.build(context);

            return Response.ok(new JsonDirectory(dir), MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            //e.printStackTrace();
            return Response.status(400).entity("Server error.").build();
        } finally {
            session.close();
        }
    }

    @PUT
    @Path("/{id}")
    public Response update(JsonDirectory dir, @Context ContainerRequestContext crc, @PathParam("id") int id) {
        int userId = (int) crc.getProperty("userId");
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        FileHelper fh = new FileHelper();

        try {
            if ( !fh.isCorrectPobjectName(dir.getTitle()) ) throw new FileException("Incorrect directory name.");

            User user = (User) session.get(User.class, userId);
            Directory dbDir = (Directory) session.get(Directory.class, id);

            // Create a new dir if not existing
            if (dbDir == null) {
                Directory parent = null;
                if (dir.getDirectory().getTitle() != null && dir.getDirectory().getTitle().equals("root")) {
                    parent = user.getRoot();
                } else if (dir.getDirectory() != null && dir.getDirectory().getId() != null) {
                    parent = (Directory) session.get(Directory.class, dir.getDirectory().getId());
                }

                if (parent == null) {
                    return Response.status(404).entity(new Error("Parent directory not found.")).type(MediaType.APPLICATION_JSON).build();
                } else if (parent.getUser() != user && !user.canEdit(dbDir)) {
                    return Response.status(403).entity(new Error("Forbidden.")).type(MediaType.APPLICATION_JSON).build();
                } else {
                    dbDir = new Directory();
                    dbDir.setTitle(dir.getTitle());
                    dbDir.setUser(user);
                    dbDir.setDirectory(parent);
                    if (user.getRoot() != parent) {
                        dbDir.inheritPermissions(session);
                    }
                }
            } else if (dbDir.getUser() != user && !user.canEdit(dbDir) && dbDir.getDirectory().getUser() != user) {
                return Response.status(403).entity(new Error("Forbidden.")).type(MediaType.APPLICATION_JSON).build();
            }

            // New permissions
            dbDir.updatePermissions(dir, session);
            for (Pobject po : dbDir.getPobjects()) {
                po.inheritPermissions(session);
            }

            // New title
            if (!dir.getTitle().equals(dbDir.getTitle())) {
                dbDir.setTitle(dir.getTitle());
            }

            // New parent
            if (dir.getDirectory().getId() == null) {
                return Response.status(404).entity(new Error("Parent directory not found.")).type(MediaType.APPLICATION_JSON).build();
            } else if (dir.getDirectory().getId() != dbDir.getDirectory().getId()) {
                Directory parent = (Directory) session.get(Directory.class, dir.getDirectory().getId());
                dbDir.setDirectory(parent);
            }

            session.save(dbDir);
            session.getTransaction().commit();

            return Response.ok(new JsonDirectory(dbDir), MediaType.APPLICATION_JSON).build();
        } catch(FileException e) {
            //e.printStackTrace();
            return Response.status(e.getCode()).entity(new Error(e.getMessage())).type(MediaType.APPLICATION_JSON).build();
        } catch(Exception e) {
            //e.printStackTrace();
            return Response.status(400).entity(new Error("Server error.")).type(MediaType.APPLICATION_JSON).build();
        } finally {
            session.close();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id, @Context ContainerRequestContext crc) {
        int userId = (int) crc.getProperty("userId");
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        try {
            User user = (User) session.get(User.class, userId);
            Directory dir = (Directory) session.get(Directory.class, id);

            if (dir == null) {
                return Response.status(404).entity(new Error("Directory not found.")).type(MediaType.APPLICATION_JSON).build();
            } else if (dir.getUser() != user) {
                return Response.status(403).entity(new Error("Forbidden.")).type(MediaType.APPLICATION_JSON).build();
            }

            dir.build(context);
            dir.delete();
            dir.getDirectory().getPobjects().remove(dir);

            session.saveOrUpdate(dir.getDirectory());
            session.delete(dir);
            session.getTransaction().commit();

            return Response.noContent().status(200).build();
        } catch(Exception e) {
            //e.printStackTrace();
            return Response.status(400).entity(new Error("Server error.")).type(MediaType.APPLICATION_JSON).build();
        } finally {
            session.close();
        }
    }

    /**
    *   FILES
    */

    @POST
    @Path("/files")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createFile(JsonFile file, @Context ContainerRequestContext crc) {
        int userId = (int) crc.getProperty("userId");
        FileHelper fh = new FileHelper();
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        try {
            if ( !fh.isCorrectPobjectName(file.getTitle()) ) throw new FileException("Incorrect file name.");
            if ( !fh.isCorrectFileExtension(file.getTitle()) ) throw new FileException("Incorrect file extension.");

            User user = (User) session.get(User.class, userId);

            Directory parent = null;
            if (file.getDirectory() != null && file.getDirectory().getId() != null) {
                parent = (Directory) session.get(Directory.class, file.getDirectory().getId());
            } else if (file.getDirectory() != null && file.getDirectory().getTitle().equals("root")) {
                parent = user.getRoot();
            }

            if (parent == null) {
                return Response.status(404).entity(new Error("Parent directory not found.")).type(MediaType.APPLICATION_JSON).build();
            } else if (parent.getUser().getId() != userId && !parent.canBeEditedBy(userId) && !parent.canBeViewedBy(userId)) {
                return Response.status(403).entity(new Error("Forbidden.")).type(MediaType.APPLICATION_JSON).build();
            }

            File dbFile = new File();
            dbFile.setUser(user);

            File oldFile = file.getId() == null ? null : (File) session.get(File.class, file.getId());
            if (oldFile != null) {
                oldFile.build(context);
                oldFile.loadContent();
                if (oldFile.getUser() == user || user.canEdit(oldFile) || user.canView(oldFile) || oldFile.getDirectory().getUser() == user) {
                    dbFile.setContent(oldFile.getContent());
                } else {
                    return Response.status(403).entity(new Error("Forbidden.")).type(MediaType.APPLICATION_JSON).build();
                }
            } else {
                dbFile.setContent(null);
            }

            dbFile.setTitle(file.getTitle());
            dbFile.setPublished(false);
            dbFile.setDirectory(parent);
            if (user.getRoot() != parent) {
                dbFile.inheritPermissions(session);
            }
            dbFile.setLastModified(new Date());
            dbFile.setModifiedBy(user);

            session.save(dbFile);
            session.getTransaction().commit();

            dbFile.build(context);
            dbFile.save();

            return Response.ok(new JsonFile(dbFile)).type(MediaType.APPLICATION_JSON).build();
        } catch(FileException e) {
            //e.printStackTrace();
            return Response.status(e.getCode()).entity(new Error(e.getMessage())).type(MediaType.APPLICATION_JSON).build();
        } catch(Exception e) {
            //e.printStackTrace();
            return Response.status(400).entity(new Error("Server error.")).type(MediaType.APPLICATION_JSON).build();
        } finally {
            session.close();
        }
    }

    @GET
    @Path("/files/{id}")
    public Response getFile(@Context ContainerRequestContext crc, @PathParam("id") int id) {
        int userId = (int) crc.getProperty("userId");
        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            File file = (File) session.get(File.class, id);

            if (!file.canBeViewedBy(userId) && file.getDirectory().getUser().getId() != userId) {
                return Response.status(403).entity(new Error("Forbidden.")).type(MediaType.APPLICATION_JSON).build();
            }

            file.build(context);
            file.loadMD5Hash();
            file.loadContent();

            return Response.ok(new JsonFile(file), MediaType.APPLICATION_JSON).build();
        } catch (NullPointerException|FileNotFoundException e) {
            //e.printStackTrace();
            return Response.status(404).entity(new Error("File not found.")).type(MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            //e.printStackTrace();
            return Response.status(400).entity("Server error.").build();
        } finally {
            session.close();
        }
    }

    @PUT
    @Path("/files/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateFile(JsonFile file, @Context ContainerRequestContext crc) {
        int userId = (int) crc.getProperty("userId");
        FileHelper fh = new FileHelper();
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        try {
            if ( !fh.isCorrectPobjectName(file.getTitle()) ) throw new FileException("Incorrect file name.");
            if ( !fh.isCorrectFileExtension(file.getTitle()) ) throw new FileException("Incorrect file extension.");

            User user = (User) session.get(User.class, userId);
            boolean changesMade = false;

            Directory parent = null;
            if (file.getDirectory() != null && file.getDirectory().getId() != null) {
                parent = (Directory) session.get(Directory.class, file.getDirectory().getId());
            } else if (file.getDirectory().getTitle() != null && file.getDirectory().getTitle().equals("root")) {
                parent = user.getRoot();
            }

            if (parent == null) {
                Error er = new Error("Parent directory not found.");
                return Response.status(404).entity(er).type(MediaType.APPLICATION_JSON).build();
            }

            File dbFile = null;
            if (file.getId() != null) {
                dbFile = (File) session.get(File.class, file.getId());
                if (dbFile != null) {
                    dbFile.build(context);
                }
            }

            if (dbFile == null) {
                if (parent.getUser().getId() != userId && !parent.canBeEditedBy(userId) && !user.canEdit(dbFile)) {
                    Error er = new Error("Forbidden: file's directory owner must match with file's owner OR user must have edit rights for directory and file.");
                    return Response.status(403).entity(er).type(MediaType.APPLICATION_JSON).build();
                }
                dbFile = new File();
                dbFile.setUser(user);
                dbFile.setContent(file.getContent());
                dbFile.setTitle(file.getTitle());
                dbFile.setPublished(false);
                dbFile.setDirectory(parent);
                dbFile.setLastModified(new Date());
                dbFile.setModifiedBy(user);
                
                if (user.getRoot() != parent) {
                    dbFile.inheritPermissions(session);
                }

                session.save(dbFile);

                dbFile.build(context);
                dbFile.save();
            }

            // If existing file can't be edited by user
            if (dbFile.getUser().getId() != userId && !dbFile.getDirectory().canBeEditedBy(userId) && !user.canEdit(dbFile)) {
                Error er = new Error("Forbidden: user is not owner and has no permissions.");
                return Response.status(403).entity(er).type(MediaType.APPLICATION_JSON).build();
            }

            // New content
            if (file.getContent() != null && file.getContent() != dbFile.getContent()) {
                dbFile.setContent(file.getContent());
                dbFile.setMD5Hash(file.getMD5Hash());
                dbFile.save();
                if (!changesMade) changesMade = true;
            }

            // If owner OR someone with edit rights for the file's parent folder
            if (dbFile.getUser().getId() == userId || user.canEdit(dbFile) || (user.canEdit(dbFile) && parent.canBeEditedBy(userId)) || dbFile.getDirectory().canBeEditedBy(userId)) {
                // New title
                if (!file.getTitle().equals(dbFile.getTitle()) && (parent.canBeEditedBy(userId) || user.canEdit(dbFile) || file.getUser().getId() == userId && dbFile.getUser().getId() == userId)) {
                    dbFile.setTitle(file.getTitle());
                    if (!changesMade) changesMade = true;
                }

                // New parent
                if (file.getDirectory().getId() != dbFile.getDirectory().getId()) {
                    dbFile.setDirectory(parent);
                }

                // New permissions
                if (file.getId().equals(dbFile.getId())) {
                    boolean permissionsUpdated = dbFile.updatePermissions(file, session);
                    if (!changesMade && permissionsUpdated) changesMade = true;
                }

                // New publish value
                if (file.getPublished() != dbFile.getPublished()) {
                    if (file.getPublished()) {
                        RandomStringGenerator gen = new RandomStringGenerator();
                        dbFile.setUri(gen.getUri());
                        dbFile.setPublished(true);
                    } else {
                        dbFile.setUri(null);
                        dbFile.setPublished(false);
                    }
                    if (!changesMade) changesMade = true;
                }
            }

            if (changesMade) {
            	dbFile.setLastModified(new Date());
            	dbFile.setModifiedBy(user);
            }

            session.save(dbFile);
            session.getTransaction().commit();

            return Response.status(200).entity(new JsonFile(dbFile)).type(MediaType.APPLICATION_JSON).build();
        } catch (NullPointerException|FileNotFoundException e) {
            //e.printStackTrace();
            return Response.status(404).entity(new Error("File not found.")).type(MediaType.APPLICATION_JSON).build();
        } catch(FileException e) {
            //e.printStackTrace();
            return Response.status(e.getCode()).entity(new Error(e.getMessage())).type(MediaType.APPLICATION_JSON).build();
        } catch(Exception e) {
            //e.printStackTrace();
            return Response.status(400).entity(new Error("Server error.")).type(MediaType.APPLICATION_JSON).build();
        } finally {
            session.close();
        }

    }

    @DELETE
    @Path("/files/{id}")
    public Response deleteFile(@PathParam("id") int id, @Context ContainerRequestContext crc) {
        int userId = (int) crc.getProperty("userId");
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        try {
            File file = (File) session.get(File.class, id);
            if (file.getUser().getId() != userId && !file.canBeEditedBy(userId) && (file.getDirectory()).getUser().getId() != userId) {
                return Response.status(403).entity(new Error("Forbidden.")).type(MediaType.APPLICATION_JSON).build();
            }
            file.build(context);
            file.delete();
            session.delete(file);
            session.getTransaction().commit();

            return Response.noContent().status(200).build();
        } catch(Exception e) {
            //e.printStackTrace();
            return Response.status(400).entity(new Error("Server error.")).type(MediaType.APPLICATION_JSON).build();
        } finally {
            session.close();
        }
    }

    @GET
    @PermitAll
    @Path("/files/public/{uri}")
    public Response getPublicFile(@PathParam("uri") String uri) {
        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            Criteria cr = session.createCriteria(File.class);
            cr.add(Restrictions.eq("published", true));
            cr.add(Restrictions.eq("uri", uri));
            File dbFile = (File) cr.list().get(0);

            dbFile.build(context);
            dbFile.loadContent();

            JsonFile jsonFile = new JsonFile(dbFile);
            jsonFile.removeSensitiveData();

            return Response.ok(jsonFile, MediaType.APPLICATION_JSON).build();
        } catch (NullPointerException|FileNotFoundException|IndexOutOfBoundsException e) {
            //e.printStackTrace();
            return Response.status(404).entity(new Error("File not found.")).type(MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            //e.printStackTrace();
            return Response.status(400).entity("Server error.").build();
        } finally {
            session.close();
        }
    }
    
    @DELETE
    @Path("/files/permissions/{id}")
    public Response deleteUserPermissions(@PathParam("id") int id, @Context ContainerRequestContext crc) {
    	
        int userId = (int) crc.getProperty("userId");
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        try {
        	
        	File file = (File) session.get(File.class, id);
        	file.deleteUserPermissions(session, userId);
        	
            session.getTransaction().commit();
        	
            return Response.noContent().status(200).build();
            
        } catch(Exception e) {
        	
            //e.printStackTrace();
            return Response.status(400).entity(new Error("Server error.")).type(MediaType.APPLICATION_JSON).build();
            
        } finally {
        	
            session.close();
            
        }
        
    }
    
}
