package com.naples.rest;

import com.naples.file.File;
import com.naples.generator.RandomStringGenerator;
import com.naples.helper.Error;
import com.naples.json.JsonFile;
import com.naples.util.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import javax.annotation.security.PermitAll;
import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;

@Path("/view")
public class PublishService {

    @Context
    private ServletContext context;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createPublished(File file, @Context ContainerRequestContext crc) {
        int userId = (int) crc.getProperty("userId");

        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        try {
            File dbFile = null;
            if (file.getId() != null) {
                dbFile = (File) session.get(File.class, file.getId());
            }

            if (dbFile.getUser().getId() != userId) {
                return Response.status(403).entity(new Error("Forbidden.")).type(MediaType.APPLICATION_JSON).build();
            }

            dbFile.setPublished(true);

            RandomStringGenerator gen = new RandomStringGenerator();
            dbFile.createUri(gen);

            return Response.ok(new JsonFile(dbFile), MediaType.APPLICATION_JSON).build();

        }catch(Exception e) {
            e.printStackTrace();
            return Response.status(400).entity(new Error("Server error.")).type(MediaType.APPLICATION_JSON).build();
        } finally {
            session.getTransaction().commit();
            session.close();
        }
    }

    @GET
    @PermitAll
    @Path("/{uri}")
    public Response openPublished(@PathParam("uri") String uri) {
        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            Criteria cr = session.createCriteria(File.class);
            cr.add(Restrictions.eq("published", true));
            cr.add(Restrictions.eq("uri", uri));
            File dbFile = (File) cr.list().get(0);

            dbFile.build(context);
            dbFile.loadContent();

            return Response.ok(new JsonFile(dbFile), MediaType.APPLICATION_JSON).build();
        } catch (NullPointerException|FileNotFoundException|IndexOutOfBoundsException e) {
            e.printStackTrace();
            return Response.status(404).entity(new Error("File not found.")).type(MediaType.APPLICATION_JSON).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(400).entity("Server error.").build();
        } finally {
            session.close();
        }
    }


    @DELETE
    @Path("/{uri}")
    public Response delete(@PathParam("uri") String uri, @Context ContainerRequestContext crc) {
        int userId = (int) crc.getProperty("userId");

        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        try {
            Criteria cr = session.createCriteria(File.class);
            cr.add(Restrictions.eq("published", true));
            cr.add(Restrictions.eq("uri", uri));
            File file = (File) cr.list().get(0);

            if (file.getUser().getId() != userId) {
                return Response.status(403).entity(new Error("Forbidden.")).type(MediaType.APPLICATION_JSON).build();
            }

            file.setPublished(false);
            file.setUri(null);

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
