package com.naples.rest;

import java.util.Set;
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
import com.naples.file.FileList;
import com.naples.file.FileHelper;
import com.naples.file.FileException;
import com.naples.user.User;
import com.naples.helper.Error;
import com.naples.helper.Success;

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
            Set<File> files = user.getFiles();

            for (File file : files) {
                file.setUser(null);
                file.build(context);
                file.loadLastModified();
            }

            return Response.ok(new FileList(files), MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            e.printStackTrace();
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
            if (file.getUser().getId() != userId) {
                return Response.status(403).entity(new Error("Forbidden.")).type(MediaType.APPLICATION_JSON).build();
            }
            file.setUser(null); // JSON serializer will go crazy otherwise
            file.build(context);
            file.loadMD5Hash();
            file.loadContent();
            file.loadLastModified();
            return Response.ok(file, MediaType.APPLICATION_JSON).build();
        } catch (NullPointerException|FileNotFoundException e) {
            e.printStackTrace();
            return Response.status(404).entity(new Error("File not found.")).type(MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(400).entity("Server error.").build();
        } finally {
            session.close();
        }

    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response save(File file, @Context ContainerRequestContext crc) {
        int userId = (int) crc.getProperty("userId");

        FileHelper fh = new FileHelper();
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        try {
            if ( !fh.isCorrectFileName(file.getTitle()) ) throw new FileException("Incorrect file name.");
            if ( !fh.isCorrectFileExtension(file.getTitle()) ) throw new FileException("Incorrect file extension.");

            User user = (User) session.get(User.class, userId);

            File dbFile = null;
            if (file.getId() != null) {
                dbFile = (File) session.get(File.class, file.getId());
            }

            // No file in DB
            if (dbFile == null) {
                file.setUser(user);
                session.save(file);
                file.build(context);
                file.save();
            // File in DB but is not owner. TODO: Change when file rights implemented.
            } else if (dbFile.getUser().getId() != userId) {
                return Response.status(403).entity(new Error("Forbidden.")).type(MediaType.APPLICATION_JSON).build();
            // File in DB but new name.
            } else if ( !dbFile.getTitle().equals(file.getTitle()) ) {
                file.setUser(user);
                session.save(file);
                file.build(context);
                file.save();
            // File in DB
            } else {
                file.setUser(user);
                file.build(context);
                file.save();
            }

            return Response.status(200).entity(new Success(file.getMD5Hash())).type(MediaType.APPLICATION_JSON).build();
        } catch(FileException e) {
            //e.printStackTrace();
            System.out.println("User tried to save file but content has changed.");
            return Response.status(e.getCode()).entity(new Error(e.getMessage())).type(MediaType.APPLICATION_JSON).build();
        } catch(Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
            return Response.status(400).entity(new Error("Server error.")).type(MediaType.APPLICATION_JSON).build();
        } finally {
            session.getTransaction().commit();
            session.close();
        }

    }

}