package com.naples.rest;

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

import org.hibernate.Session;
import org.hibernate.Filter;

import com.naples.util.HibernateUtil;
import com.naples.file.File;
import com.naples.file.Files;
import com.naples.helper.Error;
import com.naples.helper.Success;
import com.naples.helper.FileHelper;
import com.naples.helper.FileException;

@Path("/files")
public class FileService {

    @Context
    private ServletContext context;

    @GET
    public Response list() {
        /**
        * TODO:
        * Get user from securityContext, check user rights and:
        * If no rights, return 403 (forbidden)
        * If rights, return 200 (ok) with file.
        */

        FileHelper fh = new FileHelper();

        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        Filter filter = session.enableFilter("userFilter");
        filter.setParameter("userFilterParam", 1);
        filter.validate();

        try {
            List<File> files = (List<File>) session.createCriteria(File.class).list();
            String bpmnFilesDir = this.context.getRealPath(this.context.getInitParameter("bpmn-files-dir"));

            for (File file : files) {
                String filePathStr = bpmnFilesDir + "/" + file.getTitle();
                java.nio.file.Path filePath = Paths.get(filePathStr);
                file.setLastModified(fh.getFileLastModifiedString(filePath));
            }

            return Response.ok(new Files(files), MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(400).entity("Server error.").build();
        } finally {
            session.close();
        }

    }

    @GET
    @Path("/{id}")
    public Response open(@PathParam("id") int id) {
        /**
        * TODO:
        * Get user from securityContext, check user rights and:
        * If no rights, return 403 (forbidden)
        * If rights, return 200 (ok) with file.
        */

        FileHelper fh = new FileHelper();
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        try {
            File file = (File) session.get(File.class, id);
            String bpmnFilesDir = this.context.getRealPath(this.context.getInitParameter("bpmn-files-dir"));
            String filePathStr = bpmnFilesDir + "/" + file.getTitle();
            java.nio.file.Path filePath = Paths.get(filePathStr);
            file.setLastModified(fh.getFileLastModifiedString(filePath));
            file.setMD5Hash(fh.getMD5Hash(filePathStr));
            file.setContent(fh.getContent(filePath));

            return Response.ok(file, MediaType.APPLICATION_JSON).build();
        } catch (NullPointerException|FileNotFoundException e) {
            System.out.println("ERROR: Client requested a file with ID: " + id + " which does not exist.");
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
    public Response save(File file) {
        /**
        * TODO:
        * Get user from securityContext, check user rights and:
        * If no rights, return 403 (forbidden)
        * If md5 hash conflict, return 409 (conflict).
        * If no conlict, save file and return 200/201 (ok/created).
        */
        FileHelper fh = new FileHelper();
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        try {
            if ( !fh.isCorrectFileName(file.getTitle()) ) throw new FileException("Incorrect file name.");
            if ( !fh.isCorrectFileExtension(file.getTitle()) ) throw new FileException("Incorrect file extension.");

            File dbFile = (File) session.get(File.class, file.getId());

            String bpmnFilesDir = this.context.getRealPath(this.context.getInitParameter("bpmn-files-dir"));
            String filePathStr = bpmnFilesDir + "/" + file.getTitle();
            fh.saveFile(file.getContent(), file.getMD5Hash(), filePathStr);
            String fileMD5 = fh.getMD5Hash(filePathStr);

            // No file in DB
            if (dbFile == null) {
                session.save(file);
            // File in DB but new name.
            } else if ( !dbFile.getTitle().equals(file.getTitle()) ) {
                session.save(file);
            }

            return Response.status(201).entity(new Success(fileMD5)).type(MediaType.APPLICATION_JSON).build();
        } catch(FileException e) {
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
    public Response delete(@PathParam("id") int id) {
        /**
        * TODO:
        * Get user from securityContext, check user rights and perform the correct operation:
        * Owner - delete file permanently, return 205 (reset content)
        * User - delete users permissions for the file, return 205 (reset content)
        */
        FileHelper fh = new FileHelper();
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        try {
            File file = (File) session.get(File.class, id);
            String bpmnFilesDir = this.context.getRealPath(this.context.getInitParameter("bpmn-files-dir"));
            String filePathStr = bpmnFilesDir + "/" + file.getTitle();
            fh.deleteFile(filePathStr);

            return Response.noContent().status(200).build();
        } catch(Exception e) {
            return Response.status(400).entity(new Error("Server error.")).type(MediaType.APPLICATION_JSON).build();
        } finally {
            session.close();
        }

    }

}