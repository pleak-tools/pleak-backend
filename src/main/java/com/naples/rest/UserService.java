package com.naples.rest;

import java.util.List;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hibernate.Filter;
import org.hibernate.Session;

import com.naples.user.User;
import com.naples.user.UserExtension;
import com.naples.user.UserHelper;
import com.naples.util.HibernateUtil;

@Path("/user")
public class UserService {

  @POST
  @Path("/exists")
  @PermitAll
  @Consumes(MediaType.APPLICATION_JSON)
  public Response exists(User user) {
    Session session = HibernateUtil.getSessionFactory().openSession();
    session.beginTransaction();
    Filter filter = session.enableFilter("userFilterByEmail");
    filter.setParameter("userFilterParam", user.getEmail());

    try {
      filter.validate();

      List<User> users = (List<User>) session.createCriteria(User.class).list();
      if (users.size() == 0) {
        return Response.status(404).entity(new Error("User not found.")).build();
      }
      User dbUser = (User) users.get(0);

      return Response.ok().build();
    } catch (Exception e) {
      e.printStackTrace();
      return Response.status(409).entity("Server error.").build();
    } finally {
      session.getTransaction().commit();
      session.close();
    }

  }

  @PUT
  @Path("/password")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response changePassword(@Context ContainerRequestContext crc, UserExtension userExt) {
    int userId = (int) crc.getProperty("userId");
    Session session = HibernateUtil.getSessionFactory().openSession();
    session.beginTransaction();
    User user = (User) session.get(User.class, userId);
    UserHelper uh = new UserHelper();

    try {

      if (userExt.getCurrentPassword() == null) {
        return Response.status(403).entity(new Error("Wrong password.")).build();
      } else if (!uh.isValidPassword(userExt.getNewPassword())) {
        return Response.status(400).entity(new Error("Invalid password.")).build();
      }

      if (user.changePassword(userExt.getCurrentPassword(), userExt.getNewPassword())) {
        session.save(user);
        return Response.noContent().status(200).build();
      } else {
        return Response.status(403).entity(new Error("Wrong password.")).build();
      }

    } catch (Exception e) {
      e.printStackTrace();
      return Response.status(409).entity("Server error.").build();
    } finally {
      session.getTransaction().commit();
      session.close();
    }

  }

}
