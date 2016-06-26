package com.naples.rest;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.nio.file.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileNotFoundException;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import javax.ws.rs.container.ContainerRequestContext;

import org.hibernate.Session;
import org.hibernate.Filter;

import com.naples.util.HibernateUtil;
import com.naples.file.File;
import com.naples.file.FileHelper;
import com.naples.file.FileException;
import com.naples.file.FilePermission;
import com.naples.user.User;
import com.naples.helper.Action;
import com.naples.helper.Error;
import com.naples.helper.Success;
import com.naples.json.JsonFile;
import com.naples.json.JsonFileList;
import com.naples.json.JsonFilePermission;
import com.naples.json.JsonUser;

@Path("/files")
public class FileService {

    @Context
    private ServletContext context;

    @GET
    public Response list(@Context ContainerRequestContext crc) {
        FileHelper fh = new FileHelper();
        int userId = (int) crc.getProperty("userId");
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        try {
            User user = (User) session.get(User.class, userId);
            Set<File> files = user.getAllFiles();

            for (File file : files) {
                file.build(context);
                file.loadLastModified();
            }

            return Response.ok(new JsonFileList(files), MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            //e.printStackTrace();
            return Response.status(400).entity("Server error.").build();
        } finally {
            session.close();
        }

    }

    @GET
    @Path("/{id}")
    public Response open(@PathParam("id") int id, @Context ContainerRequestContext crc) {
        int userId = (int) crc.getProperty("userId");
        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            File file = (File) session.get(File.class, id);
            if (!file.canBeViewedBy(userId)) {
                return Response.status(403).entity(new Error("Forbidden.")).type(MediaType.APPLICATION_JSON).build();
            }

            file.build(context);
            file.loadMD5Hash();
            file.loadContent();
            file.loadLastModified();

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

    @POST
    @Path("/{id}/permissions")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response permissions(JsonFile file, @Context ContainerRequestContext crc) {
        int userId = (int) crc.getProperty("userId");

        FileHelper fh = new FileHelper();
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        try {
            if ( !fh.isCorrectFileName(file.getTitle()) ) throw new FileException("Incorrect file name.");
            if ( !fh.isCorrectFileExtension(file.getTitle()) ) throw new FileException("Incorrect file extension.");

            File dbFile = null;
            if (file.getId() != null) {
                dbFile = (File) session.get(File.class, file.getId());
            }

            if (dbFile != null) {
                User user = (User) session.get(User.class, userId);
                if (dbFile.getUser().getId() != userId) {
                    return Response.status(403).entity(new Error("Forbidden.")).type(MediaType.APPLICATION_JSON).build();
                }

                // Add/update existing permissions
                Set<FilePermission> existingPermissions = new HashSet<FilePermission>(0);
                List<Action> allActions = (List<Action>)session.createCriteria(Action.class).list();
                for (JsonFilePermission jfp : file.getFilePermissions()) {
                    for (Action a : allActions) {
                        if (jfp.getAction().getTitle().equals(a.getTitle())) {
                            Filter filter = session.enableFilter("userFilterByEmail");
                            filter.setParameter("userFilterParam", jfp.getUser().getEmail());
                            List<User> users = (List<User>) session.createCriteria(User.class).list();
                            User dbUser = (User) users.get(0);
                            session.disableFilter("userFilterByEmail");

                            boolean isExistingPermission = false;
                            for (FilePermission fp : dbFile.getFilePermissions()) {
                                if (fp.getUser() == dbUser) {
                                    isExistingPermission = true;
                                    fp.setAction(a);
                                    existingPermissions.add(fp);
                                    break;
                                }
                            }
                            if (!isExistingPermission) {
                                FilePermission newPermission = new FilePermission();
                                newPermission.setFile(dbFile);
                                newPermission.setUser(dbUser);
                                newPermission.setAction(a);
                                dbFile.getFilePermissions().add(newPermission);
                                existingPermissions.add(newPermission);
                            }
                        }
                    }
                }
                // Remove permissions that don't exist anymore
                for (FilePermission fp : dbFile.getFilePermissions()) {
                    if (!existingPermissions.contains(fp)) {
                        dbFile.getFilePermissions().remove(fp);
                        session.delete(fp);
                    }
                }

                session.save(dbFile);
            } else {
                return Response.status(404).entity(new Error("File not found.")).type(MediaType.APPLICATION_JSON).build();
            }

            return Response.status(200).entity(new Success(dbFile.getMD5Hash(), dbFile.getId())).type(MediaType.APPLICATION_JSON).build();
        } catch(Exception e) {
            //e.printStackTrace();
            return Response.status(400).entity(new Error("Server error.")).type(MediaType.APPLICATION_JSON).build();
        } finally {
            session.getTransaction().commit();
            session.close();
        }

    }

    @GET
    @Path("/{id}/{token}")
    public Response openPublic(@PathParam("id") int id,
                               @PathParam("token") String token,
                               @Context ContainerRequestContext crc) {
        int userId = (int) crc.getProperty("userId");
        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            File file = (File) session.get(File.class, id);
            if (file.getUser().getId() != userId || file.verifyPublicToken(token)) {
                return Response.status(403).entity(new Error("Forbidden.")).type(MediaType.APPLICATION_JSON).build();
            }

            file.build(context);
            file.loadMD5Hash();
            file.loadContent();
            file.loadLastModified();

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

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response save(JsonFile file, @Context ContainerRequestContext crc) {
        int userId = (int) crc.getProperty("userId");

        FileHelper fh = new FileHelper();
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        try {
            if ( !fh.isCorrectFileName(file.getTitle()) ) throw new FileException("Incorrect file name.");
            if ( !fh.isCorrectFileExtension(file.getTitle()) ) throw new FileException("Incorrect file extension.");

            User user = (User) session.get(User.class, userId);

            File newFile = null;
            File dbFile = null;
            if (file.getId() != null) {
                dbFile = (File) session.get(File.class, file.getId());
            }

            // No file in DB
            if (dbFile == null) {
                dbFile = new File();
                dbFile.setUser(user);
                dbFile.setContent(file.getContent());
                dbFile.setTitle(file.getTitle());
                dbFile.setPublished(false);
                session.save(dbFile);
                dbFile.build(context);
                dbFile.save();
            // Some other user is trying to edit the file
            } else if (dbFile.getUser().getId() != userId && !user.canEdit(dbFile)) {
                return Response.status(403).entity(new Error("Forbidden.")).type(MediaType.APPLICATION_JSON).build();
            // File in DB but new name.
            } else if ( !dbFile.getTitle().equals(file.getTitle()) ) {
                newFile = new File();
                newFile.setTitle(file.getTitle());
                newFile.setContent(file.getContent());
                newFile.setUser(user);
                session.save(newFile);
                newFile.build(context);
                newFile.save();
            // File in DB
            } else {
                dbFile.setContent(file.getContent());
                dbFile.build(context);
                dbFile.setMD5Hash(file.getMD5Hash());
                dbFile.save();
            }

            if (newFile != null) {
                return Response.status(200).entity(new Success(newFile.getMD5Hash(), newFile.getId())).type(MediaType.APPLICATION_JSON).build();
            }
            return Response.status(200).entity(new Success(dbFile.getMD5Hash(), dbFile.getId())).type(MediaType.APPLICATION_JSON).build();
        } catch(FileException e) {
            //e.printStackTrace();
            return Response.status(e.getCode()).entity(new Error(e.getMessage())).type(MediaType.APPLICATION_JSON).build();
        } catch(Exception e) {
            //e.printStackTrace();
            return Response.status(400).entity(new Error("Server error.")).type(MediaType.APPLICATION_JSON).build();
        } finally {
            session.getTransaction().commit();
            session.close();
        }

    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") int id, @Context ContainerRequestContext crc) {
        int userId = (int) crc.getProperty("userId");
        FileHelper fh = new FileHelper();
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        try {
            File file = (File) session.get(File.class, id);
            if (file.getUser().getId() != userId) {
                return Response.status(403).entity(new Error("Forbidden.")).type(MediaType.APPLICATION_JSON).build();
            }
            file.build(context);
            file.delete();
            session.delete(file);

            return Response.noContent().status(200).build();
        } catch(Exception e) {
            //e.printStackTrace();
            return Response.status(400).entity(new Error("Server error.")).type(MediaType.APPLICATION_JSON).build();
        } finally {
            session.getTransaction().commit();
            session.close();
        }

    }

}
